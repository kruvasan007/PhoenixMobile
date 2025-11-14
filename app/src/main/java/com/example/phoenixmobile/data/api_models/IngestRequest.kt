package com.example.phoenixmobile.data.api_models

import kotlinx.serialization.Serializable

@Serializable
data class IngestRequest(
    val question: String,
    val user_id: String
)
