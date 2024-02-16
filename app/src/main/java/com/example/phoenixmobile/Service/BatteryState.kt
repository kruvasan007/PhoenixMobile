package com.example.phoenixmobile.Service

enum class BatteryState(val state: Int) {
    BATTERY_DEAD(4),
    BATTERY_COLD(7),
    BATTERY_GOOD(2),
    BATTERY_OVERHEAT(3),
    BATTERY_OVERVOLTAGE(5)
}