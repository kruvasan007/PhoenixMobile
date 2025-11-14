package com.example.phoenixmobile.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "user_data"
    private const val KEY_EXPIRES_AT = "expires_at"

    private lateinit var prefs: SharedPreferences

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkAuthState()
    }

    private fun checkAuthState() {
        val token = getToken()
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)
        val currentTime = System.currentTimeMillis() / 1000

        if (token != null && expiresAt > currentTime) {
            // Токен действителен
            val userJson = prefs.getString(KEY_USER, null)
            if (userJson != null) {
                try {
                    val user = Json.decodeFromString<User>(userJson)
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    return
                } catch (e: Exception) {
                    // Ошибка десериализации, очищаем данные
                }
            }
        }

        // Токен недействителен или отсутствует
        clearAuth()
    }

    fun saveAuth(token: String, user: User, expiresIn: Long) {
        val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn

        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, Json.encodeToString(user))
            .putLong(KEY_EXPIRES_AT, expiresAt)
            .apply()

        _currentUser.value = user
        _isAuthenticated.value = true
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)
        val currentTime = System.currentTimeMillis() / 1000

        return if (token != null && expiresAt > currentTime) {
            token
        } else {
            null
        }
    }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_EXPIRES_AT)
            .apply()

        _currentUser.value = null
        _isAuthenticated.value = false
    }

    fun logout() {
        clearAuth()
    }
}
