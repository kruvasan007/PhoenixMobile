package com.example.phoenixmobile.data

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.phoenixmobile.retrofit.RetrofitClient
import com.example.phoenixmobile.data.api_models.ReportAnswer

object ReportApi {

    private val retrofit = RetrofitClient.instance

    fun sendReport() {
        val data = ReportManager.getRequest()
        Log.d("REPORT_API", "Sending report to server: $data")

        retrofit.sendReport(data).enqueue(object : Callback<ReportAnswer> {
            override fun onResponse(call: Call<ReportAnswer>, response: Response<ReportAnswer>) {
                Log.d("REPORT_API", "HTTP status: ${response.code()}")
                Log.d("REPORT_API", "Response body: ${response.body()}")
                if (response.isSuccessful) {
                    ReportManager.setReportResult(response.body())
                } else {
                    Log.e(
                        "REPORT_API",
                        "Unsuccessful response: errorBody=${response.errorBody()?.string()}"
                    )
                    ReportManager.setReportResult(null)
                }
            }

            override fun onFailure(call: Call<ReportAnswer>, t: Throwable) {
                Log.e("REPORT_API", "Request failure: ${t.localizedMessage}", t)
                ReportManager.setReportResult(null)
            }
        })
    }
}