package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class HardWareReport(
    @SerializedName("RAM")var ram: Long ?= null,
    @SerializedName("TOTAL SPACE")var totalSpace: Long ?= null,
    @SerializedName("AVL SPACE")var availabSpace: Long ?= null,
    @SerializedName("GYROSCOPE") var gyroscope: String ?= null,
    @SerializedName("versionOS") var versionOS: String ?= null,
    @SerializedName("sdkVersion")var sdkVersion: Int ?= null,
    @SerializedName("phoneModel") var phoneModel: String ?= null,
    @SerializedName("hardware")var hardware: Boolean ?= null,
    @SerializedName("board")var board: String ?= null,
    @SerializedName("batteryState")var batteryState: Int ?= null
)
