package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class CPUReport(
    @SerializedName("FREQUENCY_CPU")var frequency: String,

    // score for the cryptography test
    @SerializedName("MARK_FOR_TEST")var mark: String
)
