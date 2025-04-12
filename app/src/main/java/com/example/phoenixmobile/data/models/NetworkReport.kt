package com.example.phoenixmobile.data.models

data class NetworkReport(
    val level: Int = -1,          // Уровень сигнала сотовой сети
    val dataStatus: Int = -1,     // Статус мобильных данных
    val simState: Int = -1,       // Состояние SIM-карты
    val GPS: Boolean = false,     // Включен ли GPS
    val bluetooth: Boolean = false, // Включен ли Bluetooth
    val errorMessage: String = ""
)