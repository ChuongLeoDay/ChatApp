package com.example.chatapp.Animation

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.chatapp.R


object ProgressDialogUtil {
    private var alertDialog: AlertDialog? = null

    fun showProgressDialog(context: Context) {
        alertDialog?.dismiss()

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_progessbar, null)

        builder.setView(view)
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun hideProgressDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }
}
