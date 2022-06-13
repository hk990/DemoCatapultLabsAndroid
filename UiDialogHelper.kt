package io.connect.app.utils

import android.app.Activity
import android.app.Dialog
import io.connect.app.R

class UiDialogHelper {
    lateinit var dialog: Dialog

    fun openProgressAlertDialog(activity: Activity): Dialog {
        return try {
            dialog = Dialog(activity)
            dialog.setContentView(R.layout.dialog_progress_bar)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            dialog.show()
            dialog
        } catch (e: Exception) {
            e.printStackTrace()
            dialog
        }
    }

    fun closeProgressAlertDialog(dialog: Dialog?) {
        try {
            dialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}