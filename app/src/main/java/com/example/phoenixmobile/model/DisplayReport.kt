package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class DisplayReport(
    @SerializedName("WIDTH")var width: Int,
    @SerializedName("HEIGHT")var heigth: Int,
    @SerializedName("DENSITY")var density: Float
)
