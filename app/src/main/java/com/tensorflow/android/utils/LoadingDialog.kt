package com.tensorflow.android.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import com.tensorflow.android.databinding.LoadingDialogBinding

class LoadingDialog(activity: AppCompatActivity) {
    private var loadingDialog: Dialog? = null
    init {
        val binding = LoadingDialogBinding.inflate(activity.layoutInflater)
        loadingDialog = Dialog(activity)
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.setContentView(binding.root)
        loadingDialog?.setCancelable(false)
    }

    fun show() {
        loadingDialog?.show()
    }

    fun hide() {
        loadingDialog?.dismiss()
    }
}