package com.example.phoenixmobile.retrofit

import com.example.phoenixmobile.data.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.97:8080/" // URL сервера

    // Интерцептор для добавления токена аутентификации
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = AuthManager.getToken()

        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        chain.proceed(request)
    }

    // Стандартный клиент (для отчётов и лёгких запросов)
    private val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()
    }

    // Клиент без авторизации (для регистрации и входа)
    private val noAuthClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    // Клиент с увеличенными таймаутами для LLM ingest (медленные ответы)
    private val chatClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // увеличен для ожидания генерации
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS) // общий лимит на весь вызов
            .addInterceptor(authInterceptor)
            .build()
    }

    val instance: RetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }

    // Отдельный сервис для чата с увеличенными таймаутами
    val chatInstance: RetrofitService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(chatClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }

    // Сервис для аутентификации (без токена авторизации)
    val authInstance: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(noAuthClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }
}