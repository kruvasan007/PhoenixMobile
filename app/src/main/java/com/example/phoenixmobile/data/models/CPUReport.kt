package com.example.phoenixmobile.data.models

data class CPUReport(
    val frequency: String = "", // Частота CPU в MHz или GHz
    val mark: String = "",       // Оценка производительности CPU или название модели
    val errorMessage: String = ""
)