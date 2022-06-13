package io.connect.app.activities.home

import android.util.Log
import android.widget.Toast
import com.google.gson.GsonBuilder
import io.connect.app.base.BaseModel
import io.connect.app.base.BasePresenter
import io.connect.app.networking.NetworkController
import io.connect.app.utils.GH
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap


class HomePresenter(view: HomeContract.View?) : BasePresenter<HomeContract.View?>(view),
    HomeContract.Actions {

    private var call: Call<ResponseBody>? = null
    private lateinit var mCall: Call<BaseModel>
    private lateinit var _call: Call<UserDetailModel>

    override fun initScreen() {
        viewUI!!.initViews()
    }

    override fun getVPNConfigFile() {

        viewUI!!.showProgressBar()

        call = NetworkController.getInstance().apiMethods.getVPNConfigFile(GH.instance.sessionId)
        call!!.enqueue(object : Callback<ResponseBody?> {

            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {

                try {
                    val certificate: String = response.body()!!.string()
                    if (response.isSuccessful) {
                        viewUI!!.updateUI(certificate)
                    } else {
                        viewUI!!.hideProgressBar()
                        viewUI!!.updateUIonError(response.message())
                    }
                }catch (e:Exception){
                    viewUI!!.hideProgressBar()
                    viewUI!!.showToast("VPN Config error"+e.message)
                    e.printStackTrace()

                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                viewUI!!.hideProgressBar()
                viewUI!!.updateUIonFailure(t.message.toString())
            }
        })

    }

    override fun getUserDetails() {

        _call = NetworkController.getInstance().apiMethods.getUserDetailRequest(GH.instance.sessionId)
        _call.enqueue(object : Callback<UserDetailModel> {

            override fun onResponse(call: Call<UserDetailModel>, response: Response<UserDetailModel>) {

                val userDetail = response.body()
                if (response.isSuccessful) {
                    viewUI!!.updateUserDetail(userDetail!!)
                }else{
                    viewUI!!.updateUIOnError1(response.message())
                }
            }

            override fun onFailure(call: Call<UserDetailModel>, t: Throwable) {
                Log.d("params", "api failure" + t.message)
            }
        })
    }

    override fun detachView() {
        if (call != null) {
            call!!.cancel()
        }
        super.detachView()
    }
}