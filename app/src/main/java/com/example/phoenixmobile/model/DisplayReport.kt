package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class DisplayReport(
    @SerializedName("width")var width: Int,
    @SerializedName("heigth")var heigth: Int,
    @SerializedName("density")var density: Float
)
