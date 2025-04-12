package com.example.phoenixmobile.model

enum class BodyCondition(val description: String) {
    NO_DAMAGE("Без дефектов"),
    SCRATCHES("Мелкие царапины"),
    SCRATCH("Глубокие царапины"),
    DENTS("Отслоение краски"),
    CHIPPED("Сколы, трещины"),
    CREW("Изгиб"),
    VM("Вмятины")
}

enum class ScreenCondition(val description: String) {
    NO_DAMAGE("Без дефектов"),
    SCRATCH("1–2 мелкие царапины"),
    SCRATCHES("Много мелких царапин"),
    PIXELS("Полосы и битые пиксели"),
    PANTS("Пятна, блики или выгорание"),
    CHIPPED("Сколы, трещины"),
}