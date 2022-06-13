package io.connect.app.activities.home

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import io.connect.app.R
import io.connect.app.activities.menu.MenuActivity
import io.connect.app.activities.utility.WebViewActivity
import io.connect.app.base.BaseActivity
import io.connect.app.databinding.ActivityHomeBinding
import io.connect.app.utils.AppConstants
import io.connect.app.utils.GH
import java.io.IOException

class HomeActivity : BaseActivity(), View.OnClickListener, HomeContract.View {

    private val activity: Activity = this@HomeActivity
    private lateinit var bi: ActivityHomeBinding
    private var vpnStart = false
    private lateinit var presenter: HomePresenter
    private lateinit var progressDialog: Dialog
    private var viewId = 0
    private lateinit var tvHeading: TextView
    private lateinit var tvServerName: TextView
    private lateinit var ivImage: ImageView
    private var isConnect: Boolean = true
    private var config: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bi = DataBindingUtil.setContentView(activity, R.layout.activity_home)
        presenter = HomePresenter(this)
        presenter.initScreen()
    }

    override fun initViews() {

        presenter.getUserDetails()
        bi.llMenu.setOnClickListener(this)
        bi.llOpenWeb.setOnClickListener(this)
        bi.ivConnect.setOnClickListener(this)
        bi.llDisconnect.setOnClickListener(this)
        bi.ivPlayOpenWeb.setOnClickListener(this)
        bi.btnSubmit.setOnClickListener(this)

        isServiceRunning()
        VpnStatus.initLogCache(this.cacheDir)
    }

    override fun onClick(view: View?) {
        viewId = view!!.id
        when (viewId) {
            R.id.llMenu -> {
                switchActivity(MenuActivity::class.java)
            }
            R.id.llOpenWeb -> {
                startActivity(
                    Intent(activity, WebViewActivity::class.java).putExtra(
                        AppConstants.KEY_DATA,
                        AppConstants.WEB
                    )
                )
            }
            R.id.ivConnect -> {
                if (checkConnection(activity))
                    presenter.getVPNConfigFile()
            }
            R.id.llDisconnect -> {
                stopVpn()
               // bi.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
            R.id.btnSubmit -> {
                if (bi.rbRating.rating <= 3) {
                    bi.llRateServerConnection.visibility = View.GONE
                    bi.llRatingFeedback.visibility = View.VISIBLE
                }else{
                    // integrate api
                }
            }
            R.id.btnSubmitFeedback -> {
                // integrate api
               // bi.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            R.id.tvSkip ->{
                bi.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            R.id.ivPlayOpenWeb -> {
                startActivity(
                    Intent(activity, WebViewActivity::class.java).putExtra(
                        AppConstants.KEY_DATA,
                        AppConstants.WEB
                    )
                )
            }
            else -> {

            }
        }
    }

    private fun prepareVpn() {
        try {
            if (!vpnStart) {
                val intent = VpnService.prepare(activity)
                if (intent != null) {
                    resultLauncher.launch(intent)
                } else startVpn(config)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressBar()
            Toast.makeText(
                activity,
                "Unable to connect at this moment, please try again later " + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            updateVPNStatus(AppConstants.CONNECT)
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressBar()
        }
        return false
    }

    override fun updateUI(certificate: String) {

        tvHeading.text = getString(R.string.hold)
        ivImage.setImageResource(R.drawable.img_hold)
        ivImage.alpha = 0.0f
        ivImage.animate()?.setDuration(500)?.alpha(1.0f)
        config = certificate
        if (!vpnStart) {
            prepareVpn()
        }
    }

    override fun updateUIOnError1(message: String) {
        logout()
    }

    override fun updateUserDetail(userDetailModel: UserDetailModel) {
        easyPreference.addString(GH.KEYS.BROWSER_LINK.name, userDetailModel.browserPage).save()
    }

    override fun updateUIonError(message: String) {
        showToast(getString(R.string.server_error))
        progressDialog.dismiss()
        logout()

    }

    override fun updateUIonFailure(message: String) {
        showToast(message)
        progressDialog.dismiss()
        logout()
    }

    override fun showProgressBar() {

        try {
            progressDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            progressDialog.setContentView(R.layout.dialog_connect_server)
            progressDialog.setCancelable(false)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

            tvHeading = progressDialog.findViewById(R.id.tvHeading)
            tvServerName = progressDialog.findViewById(R.id.tvServerName)
            ivImage = progressDialog.findViewById(R.id.animationView)
            val progressBar: ProgressBar = progressDialog.findViewById(R.id.progress)
            progressBar.progressTintList = ColorStateList.valueOf(Color.BLACK)

            tvHeading.text = getString(R.string.connection)
            ivImage.setImageResource(R.drawable.img_connecting)
            ivImage.animate().setDuration(500).alpha(1.0f)
            val serverName: String = getString(R.string.connecting_to)
            tvServerName.text = serverName
            progressDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun hideProgressBar() {
        try {
            if (!activity.isFinishing) progressDialog.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun isServiceRunning() {
        setStatus(OpenVPNService.getStatus())

    }

    private fun startVpn(config: String) {
        try {
            OpenVpnApi.startVpn(
                activity,
                config,
                "Germany",
                AppConstants.VPN_USER_NAME,
                AppConstants.VPN_PASSWORD,
                progressDialog
            )
            vpnStart = true
        } catch (e: IOException) {
            e.printStackTrace()
            hideProgressBar()
        } catch (e: RemoteException) {
            e.printStackTrace()
            hideProgressBar()
        }
    }

    private fun setStatus(connectionState: String?) {

        if (connectionState != null) when (connectionState) {
            AppConstants.VPN_DISCONNECTED -> {
                updateVPNStatus(AppConstants.CONNECT)
                vpnStart = false
                OpenVPNService.setDefaultStatus()
            }
            AppConstants.VPN_CONNECTED -> {
                vpnStart = true
                bi.llConnectScreen.visibility = View.GONE
                bi.llDisconnectScreen.visibility = View.VISIBLE
            }
            AppConstants.VPN_RECONNECTING -> {
                updateVPNStatus(AppConstants.CONNECTING)
            }
            AppConstants.VPN_NO_NETWORK -> {
                showToast(getString(R.string.internet_error))
                updateVPNStatus("")
            }


        }
    }

    private fun updateVPNStatus(status: String) {

        when (status) {
            AppConstants.CONNECT -> {

                isConnect = true
                bi.llDisconnectScreen.visibility = View.GONE
                bi.llDisconnect.visibility = View.GONE
                bi.llOpenWeb.visibility = View.GONE
                bi.llConnectScreen.visibility = View.VISIBLE
                easyPreference.addBoolean(GH.KEYS.IS_SERVER_ON.name, false).save()

            }
            AppConstants.CONNECTED -> {

                if (isConnect) {
                    easyPreference.addBoolean(GH.KEYS.IS_SERVER_ON.name, true).save()
                    isConnect = false
                    tvHeading.text = getString(R.string.establish)
                    tvServerName.text = getString(R.string.connected)
                    ivImage.setImageResource(R.drawable.img_establish)
                    ivImage.alpha = 0.0f
                    ivImage.animate()?.setDuration(500)?.alpha(1.0f)
                    Handler(Looper.getMainLooper()).postDelayed({
                        hideProgressBar()
                        bi.llConnectScreen.visibility = View.GONE
                        bi.llDisconnectScreen.visibility = View.VISIBLE
                        bi.llDisconnect.visibility = View.VISIBLE
                        bi.llOpenWeb.visibility = View.VISIBLE
                    }, durationTimer)
                }
            }
            else -> {
                showToast(getString(R.string.server_error))
                easyPreference.addBoolean(GH.KEYS.IS_SERVER_ON.name, false).save()
            }
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startVpn(config)
            } else {
                prepareVpn()
            }
        }

    override fun onResume() {
        LocalBroadcastManager.getInstance(activity)
            .registerReceiver(broadcastReceiver, IntentFilter(AppConstants.CONNECTION_STATE))
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(broadcastReceiver)
    }

   
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

}