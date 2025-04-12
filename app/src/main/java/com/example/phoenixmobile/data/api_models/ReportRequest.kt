package com.example.phoenixmobile.data.api_models

data class ReportRequest(
    val deviceId: String? = null,
    val screen: String? = null,
    val body: String? = null,
    val ram: Long? = null,
    val totalSpace: Long? = null,
    // not required
    val frequency: String? = null,
    val mark: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val density: Float? = null,
    val gyroscope: String? = null,
    val versionOS: String? = null,
    val batteryState: Int? = null,
    val level: Int? = null,
    val dataStatus: Int? = null,
    val gps: Boolean? = null,
    val bluetooth: Boolean? = null,
    val audioReport: Boolean? = null
)