package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class NetworkReport(
    @SerializedName("Network level")var level: Int,
    @SerializedName("Data status")var dataStatus: Int,
    @SerializedName("Sim state")var simState: Int,
    @SerializedName("GPS state")var GPS: Int,
    @SerializedName("bluetooth")var bluetooth: Boolean
)
