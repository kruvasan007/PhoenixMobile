package com.example.phoenixmobile.retrofit

import com.example.phoenixmobile.data.api_models.ReportAnswer
import com.example.phoenixmobile.data.api_models.ReportRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitService {
    @POST("reports")
    fun sendReport(@Body request: ReportRequest): Call<ReportAnswer>
}