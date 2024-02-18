package com.example.phoenixmobile.ui.device

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.phoenixmobile.R
import com.example.phoenixmobile.databinding.FragmentMyDeviceBinding
import com.example.phoenixmobile.databinding.LoadiongDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingDialog(val activity: Activity) {
    private lateinit var dialog: AlertDialog
    private lateinit var messageBoxView : View;

    fun startLoadingDialog() {
        messageBoxView = LayoutInflater.from(activity).inflate(R.layout.loadiong_dialog, null)
        val messageBoxBuilder = AlertDialog.Builder(activity).setView(messageBoxView)
        //show dialog
        dialog = messageBoxBuilder.show()
    }

    fun setMessage(msg: String) {
        messageBoxView.findViewById<TextView>(R.id.message).text = msg
    }

    fun dismissDialog() {
        dialog.dismiss()
    }

}