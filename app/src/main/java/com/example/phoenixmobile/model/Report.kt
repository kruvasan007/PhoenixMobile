package com.example.phoenixmobile.model

data class Report(
    val deviceId: String? = null,
    val aboutDevice: String? = null,
    val cpuReport: CPUReport? = null,
    val displayReport: DisplayReport? = null,
    var hardWareReport: HardWareReport? = null,
    val networkReport: NetworkReport? = null,
    val audioReport: Boolean? = false
)
