package com.example.phoenixmobile.data

object DeviceInfoManager {
    var aboutDeviceText: String = ""
        private set

    var deviceId: String = ""
        private set

    fun setDeviceId(id: String) {
        deviceId = id
    }

    fun setOSReport(info: String) {
        aboutDeviceText = info
    }
}