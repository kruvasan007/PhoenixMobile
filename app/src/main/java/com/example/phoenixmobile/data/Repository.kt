package com.example.phoenixmobile.data

import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.App
import com.example.phoenixmobile.database.ReportDao
import com.example.phoenixmobile.model.Report
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job

object Repository {
    private val reportDao: ReportDao = App.getDatabase()!!.reportDao()
    //private val retrofitService = Common.retrofitService
    private var job: Job? = null
    private val loadError = MutableLiveData<String?>()
    private val loading = MutableLiveData<Boolean>()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    fun pullReport(report: Report) {
        // reportDao.insertReport(report)
    }

    fun startCheckingDevice() {

    }

    init {
        //TODO
    }
}