package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class HardWareReport(
    @SerializedName("RAM")var ram: Long ?= 0,
    @SerializedName("TOTAL_SPACE")var totalSpace: Long ?= 0,
    @SerializedName("AVL_SPACE")var availabSpace: Long ?= 0,
    @SerializedName("GYROSCOPE") var gyroscope: String ?= "no gyro",
    @SerializedName("VERSION_OS") var versionOS: String ?= null,
    @SerializedName("SDK_VERSION")var sdkVersion: Int ?= null,
    @SerializedName("PHONE_MODEL") var phoneModel: String ?= null,
    @SerializedName("HARDWARE")var hardware: Boolean ?= false,
    @SerializedName("BOARD")var board: String ?= null,
    @SerializedName("BATTERY_STATE")var batteryState: Int ?= 4
)
