package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class NetworkReport(
    @SerializedName("NETWORK_LEVEL") var level: Int? = -1,
    @SerializedName("DATA_STATUS") var dataStatus: Int? = -1,
    @SerializedName("SIM_STATE") var simState: Int? = -1,
    @SerializedName("GPS_STATE") var GPS: Boolean? = false,
    @SerializedName("BLUETOOTH") var bluetooth: Boolean? = false
)
