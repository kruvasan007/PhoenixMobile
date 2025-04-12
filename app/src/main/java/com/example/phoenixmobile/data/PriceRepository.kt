package com.example.phoenixmobile.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.App
import com.example.phoenixmobile.database.PriceDto
import kotlinx.coroutines.*

object PriceRepository {
    private val dao = App.getDatabase()?.priceDao()!!
    private val _priceList = MutableLiveData<List<PriceDto>>()
    val priceList: LiveData<List<PriceDto>> get() = _priceList

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun insert(price: PriceDto) {
        coroutineScope.launch {
            dao.insertModel(price)
        }
    }

    fun loadPrices() {
        coroutineScope.launch {
            val items = dao.getAll()
            withContext(Dispatchers.Main) {
                _priceList.value = items
            }
        }
    }
}