package com.example.phoenixmobile.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val fullName: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UserRegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String? = null
)

@Serializable
data class UserLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class UserLoginResponse(
    val user: User,
    val token: String,
    val expiresIn: Long // в секундах
)

@Serializable
data class AuthTokenPayload(
    val userId: Int,
    val username: String,
    val email: String,
    val exp: Long,
    val iat: Long
)
