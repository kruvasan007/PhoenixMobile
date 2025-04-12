package com.example.phoenixmobile.model

enum class BodyCondition(val description: String) {
    NO_DAMAGE("Корпус без повреждений"),
    SCRATCHES("Царапины на корпусе"),
    DENTS("Вмятины"),
    CHIPPED("Сколы"),
    OTHER("Другое")
}

enum class ScreenCondition(val description: String) {
    NO_DAMAGE("Экран без повреждений"),
    SCRATCHES("Царапины на экране"),
    CRACKS("Трещины на экране"),
    DEAD_PIXELS("Битые пиксели / пятна"),
    OTHER("Другое")
}