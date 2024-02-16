package com.example.phoenixmobile.model

import android.hardware.Sensor

data class Report(
    var id: Int? = null,
    var deviceModel: String? = null,
    var networkState: Network? = null,
    var memoryState: Memory? = null,
    var sensors: Sensors? = null,
    var system: System? = null,
    var display: Display? = null,
    var battery: Battery? = null
)

data class Network(
    var level: Int,
    var dataStatus: Int,
    var simState: Int
)

data class Memory(
    var ram: Long,
    var totalSpace: Long,
    var availabSpace: Long
)

data class Sensors(
    var sensors: List<Sensor>
)

data class System(
    var versionOS: String,
    var sdkVersion: Int,
    var phoneModel: String,
    var hardware: String,
    var board: String
)

data class Battery(
    var batteryState: Int
)

data class Display(
    var height: Int,
    var width: Int,
    var dpi: Int
)
