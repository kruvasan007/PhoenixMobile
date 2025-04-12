package com.example.phoenixmobile.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.phoenixmobile.retrofit.RetrofitClient
import com.example.phoenixmobile.data.api_models.ReportAnswer

object ReportApi {

    private val retrofit = RetrofitClient.instance

    fun sendReport() {
        val data = ReportManager.getRequest()

        retrofit.sendReport(data).enqueue(object : Callback<ReportAnswer> {
            override fun onResponse(call: Call<ReportAnswer>, response: Response<ReportAnswer>) {
                if (response.isSuccessful) {
                    ReportManager.setReportResult(response.body())
                } else {
                    ReportManager.setReportResult(null)
                }
            }

            override fun onFailure(call: Call<ReportAnswer>, t: Throwable) {
                ReportManager.setReportResult(null)
            }
        })
    }
}