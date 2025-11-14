package com.example.phoenixmobile.ui.chat

import kotlinx.serialization.Serializable

data class Triplet(
    val subject: String,
    val relation: String,
    val `object`: String
)

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
