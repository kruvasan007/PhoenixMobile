package com.example.phoenixmobile.model

data class Report(
    var id: Int? = null,
    var deviceModel: String? = null,
    var networkState: Network? = null
)

data class Network(
    var level: Int,
    var asuLevel: Int,
)
