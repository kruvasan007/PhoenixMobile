package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class HardWareReport(
    @SerializedName("RAM")var ram: Long ?= null,
    @SerializedName("TOTAL_SPACE")var totalSpace: Long ?= null,
    @SerializedName("AVL_SPACE")var availabSpace: Long ?= null,
    @SerializedName("GYROSCOPE") var gyroscope: String ?= null,
    @SerializedName("VERSION_OS") var versionOS: String ?= null,
    @SerializedName("SDK_VERSION")var sdkVersion: Int ?= null,
    @SerializedName("PHONE_MODEL") var phoneModel: String ?= null,
    @SerializedName("HARDWARE")var hardware: Boolean ?= null,
    @SerializedName("BOARD")var board: String ?= null,
    @SerializedName("BATTERY_STATE")var batteryState: Int ?= null
)
