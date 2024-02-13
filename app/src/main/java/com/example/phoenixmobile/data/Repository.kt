package com.example.phoenixmobile.data

import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.retrofit.Common
import kotlinx.coroutines.*

object Repository {
    private val retrofitService = Common.retrofitService
    private var job: Job? = null
    private val loadError = MutableLiveData<String?>()
    private val loading = MutableLiveData<Boolean>()

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    init {
        //TODO
    }
}