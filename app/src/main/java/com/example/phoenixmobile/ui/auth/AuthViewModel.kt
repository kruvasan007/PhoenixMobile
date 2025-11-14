package com.example.phoenixmobile.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.phoenixmobile.data.AuthApi
import com.example.phoenixmobile.data.AuthManager
import com.example.phoenixmobile.model.UserLoginRequest
import com.example.phoenixmobile.model.UserRegistrationRequest
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _error.value = "Заполните все поля"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val request = UserLoginRequest(username, password)
            val result = AuthApi.login(request)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                AuthManager.saveAuth(response.token, response.user, response.expiresIn)
                _authSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка входа"
            }

            _loading.value = false
        }
    }

    fun register(username: String, email: String, password: String, fullName: String?) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Заполните обязательные поля"
            return
        }

        if (!isValidEmail(email)) {
            _error.value = "Неверный формат email"
            return
        }

        if (password.length < 6) {
            _error.value = "Пароль должен содержать минимум 6 символов"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val request = UserRegistrationRequest(username, email, password, fullName)
            val result = AuthApi.register(request)

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                AuthManager.saveAuth(response.token, response.user, response.expiresIn)
                _authSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Ошибка регистрации"
            }

            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
