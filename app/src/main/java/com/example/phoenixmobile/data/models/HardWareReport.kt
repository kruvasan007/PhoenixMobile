package com.example.phoenixmobile.data.models

data class HardWareReport(
    var ram: Long = 0L,             // Объем оперативной памяти в байтах
    var totalSpace: Long = 0L,      // Весь объём памяти на устройстве
    var availabSpace: Long = 0L,    // Доступное пространство
    var gyroscope: String = "",     // Статус гироскопа: "true" / "false"
    var batteryState: Int = -1,     // Уровень заряда или статус батареи
    var versionOS: String = "",     // Версия операционной системы (например, Android 13)
    var bluetooth: Boolean = false,
    val errorMessage: String = ""
)