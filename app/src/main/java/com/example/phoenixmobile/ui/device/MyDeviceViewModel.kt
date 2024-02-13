package com.example.phoenixmobile.ui.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phoenixmobile.data.Repository

class MyDeviceViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    fun setNetworkParams(report: Map<String, String>): Boolean {
        return false
    }
}