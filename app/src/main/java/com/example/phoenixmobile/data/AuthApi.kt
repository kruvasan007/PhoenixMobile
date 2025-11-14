package com.example.phoenixmobile.data

import android.util.Log
import com.example.phoenixmobile.model.UserLoginRequest
import com.example.phoenixmobile.model.UserLoginResponse
import com.example.phoenixmobile.model.UserRegistrationRequest
import com.example.phoenixmobile.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthApi {
    private val retrofit = RetrofitClient.authInstance

    suspend fun register(request: UserRegistrationRequest): Result<UserLoginResponse> = withContext(Dispatchers.IO) {
        Log.d("AUTH_API", "Registration request started for username: ${request.username}")
        try {
            val response = retrofit.register(request)
            Log.d("AUTH_API", "Registration HTTP status=${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AUTH_API", "Registration successful for user: ${body?.user?.username}")
                if (body != null) {
                    Result.success(body)
                } else {
                    Log.e("AUTH_API", "Registration response body is null")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorMsg = "Ошибка регистрации: ${response.code()}"
                Log.e("AUTH_API", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AUTH_API", "Registration exception", e)
            Result.failure(e)
        }
    }

    suspend fun login(request: UserLoginRequest): Result<UserLoginResponse> = withContext(Dispatchers.IO) {
        Log.d("AUTH_API", "Login request started for username: ${request.username}")
        try {
            val response = retrofit.login(request)
            Log.d("AUTH_API", "Login HTTP status=${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AUTH_API", "Login successful for user: ${body?.user?.username}")
                if (body != null) {
                    Result.success(body)
                } else {
                    Log.e("AUTH_API", "Login response body is null")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorMsg = "Неверные учетные данные"
                Log.e("AUTH_API", "Login failed: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("AUTH_API", "Login exception", e)
            Result.failure(e)
        }
    }
}
