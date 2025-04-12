package com.example.phoenixmobile.data.models

data class DisplayReport(
    val screen: String = "",
    val body: String = "",
    val width: Int = 0,     // Ширина экрана в пикселях
    val height: Int = 0,    // Высота экрана в пикселях
    val density: Float = 0f, // Плотность экрана (dpi / плотность пикселей)
    val errorMessage: String = ""
)