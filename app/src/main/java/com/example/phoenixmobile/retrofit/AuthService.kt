package com.example.phoenixmobile.retrofit

import com.example.phoenixmobile.model.UserLoginRequest
import com.example.phoenixmobile.model.UserLoginResponse
import com.example.phoenixmobile.model.UserRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/register")
    suspend fun register(@Body request: UserRegistrationRequest): Response<UserLoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<UserLoginResponse>
}
