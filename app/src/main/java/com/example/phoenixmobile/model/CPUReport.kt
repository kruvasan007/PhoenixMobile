package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class CPUReport(
    @SerializedName("FREQUENCY_CPU")var frequency: String,
    @SerializedName("MARK_FOR_TEST")var mark: String
)
