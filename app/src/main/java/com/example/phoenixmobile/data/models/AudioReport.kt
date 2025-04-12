package com.example.phoenixmobile.data.models

data class AudioReport(
    val testStatus: Boolean = false,     // Статус аудио теста, например: 0 - не начат, 777 - успешно
    val errorMessage: String = "" // Сообщение об ошибке, если есть
)