package com.example.phoenixmobile.retrofit
object Common {
    private val BASE_URL = " " //TODO name server
    val retrofitService: RetrofitService
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitService::class.java)
}