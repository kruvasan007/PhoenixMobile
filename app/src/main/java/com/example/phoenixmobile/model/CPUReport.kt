package com.example.phoenixmobile.model

import com.google.gson.annotations.SerializedName

data class CPUReport(
    @SerializedName("frequencyCPU")var frequency: String,
    @SerializedName("mark for test")var mark: String
)
