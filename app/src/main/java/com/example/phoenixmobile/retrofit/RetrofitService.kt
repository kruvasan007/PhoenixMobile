package com.example.phoenixmobile.retrofit

import com.example.phoenixmobile.data.api_models.ClearResponse
import com.example.phoenixmobile.data.api_models.IngestRequest
import com.example.phoenixmobile.data.api_models.IngestResponse
import com.example.phoenixmobile.data.api_models.ReportAnswer
import com.example.phoenixmobile.data.api_models.ReportRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface RetrofitService {
    @POST("reports")
    fun sendReport(@Body request: ReportRequest): Call<ReportAnswer>

    @POST("ingest")
    suspend fun ingestQuestion(@Body req: IngestRequest): Response<IngestResponse>

    @DELETE("clear")
    suspend fun clearGraph(): Response<ClearResponse>
}