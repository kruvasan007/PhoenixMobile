package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class DisplayReport(
    @SerializedName("WIDTH") var width: Int? = -1,
    @SerializedName("HEIGHT") var heigth: Int? = -1,
    @SerializedName("DENSITY") var density: Float? = -1f
)
