package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class NetworkReport(
    @SerializedName("NETWORK_LEVEL")var level: Int,
    @SerializedName("DATA_STATUS")var dataStatus: Int,
    @SerializedName("SIM_STATE")var simState: Int,
    @SerializedName("GPS_STATE")var GPS: Boolean,
    @SerializedName("BLUETOOTH")var bluetooth: Boolean
)
