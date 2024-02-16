package com.example.phoenixmobile.ui.device

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.example.phoenixmobile.R
import com.example.phoenixmobile.databinding.LoadiongDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingDialog(val activity: Activity) {
    private lateinit var dialog: AlertDialog
    private var _binding: LoadiongDialogBinding? = null

    @SuppressLint("InflateParams")
    fun startLoadingDialog() {
        val builder = MaterialAlertDialogBuilder(activity)
        _binding = LoadiongDialogBinding.inflate(activity.layoutInflater, null, false)
        builder.setView(activity.layoutInflater.inflate(R.layout.loadiong_dialog, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
    }

    fun setMessage(msg: String) {
        dialog.setMessage(msg)
    }

    fun dismissDialog() {
        dialog.dismiss()
    }

}