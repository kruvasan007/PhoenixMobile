package com.example.phoenixmobile.ui.search

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.PriceRepository
import com.example.phoenixmobile.database.PriceDto
import com.example.phoenixmobile.model.Graphic
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.InputStreamReader
import java.util.TreeMap


class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _priceList = PriceRepository.priceList
    private val priceTable = MutableLiveData<TreeMap<String, Double>>()
    private val graphic = MutableLiveData<Graphic>()

    init {
        loadPriceList()
        loadGraphicsList()
    }

    private fun convertPriceList() {
        _priceList.observeForever {
            val map = TreeMap<String, Double>()
            for (item in it) {
                map[item.model] = item.price
            }
            priceTable.postValue(map)
        }
    }

    fun getPriceList() = priceTable
    fun getGraphicsData(id: Int) = graphic

    @SuppressLint("ResourceType")
    private fun loadGraphicsList() {
        //TODO: get data from server

        /* test */
        // parse sample file
        GlobalScope.launch(Dispatchers.IO) {
            val inputStreamReader = InputStreamReader(
                getApplication<Application>().resources.openRawResource(
                    R.raw.graphics_pattern
                )
            )
            val jsonInput = inputStreamReader.readText()
            val gson = Gson()
            graphic.postValue(gson.fromJson(jsonInput, Graphic::class.java))
        }
        /* test */
        convertPriceList()
    }

    @SuppressLint("ResourceType")
    private fun loadPriceList() {
        //TODO: get data from server


        /* test */
        // parse sample file
        GlobalScope.launch(Dispatchers.IO) {
            val inputStreamReader = InputStreamReader(
                getApplication<Application>().resources.openRawResource(
                    R.raw.configs_pattern
                )
            )
            val jObject = JSONObject(inputStreamReader.readText())
            val priceMap = TreeMap<String, Double>()
            var id = 0
            for (brand in jObject.keys()) {
                val brandOBJECT = jObject.getJSONObject(brand)
                for (item in brandOBJECT.keys()) {
                    priceMap["$brand;$item"] = brandOBJECT.get(item).toString().toDouble()
                    PriceRepository.insert(
                        PriceDto(
                            id = null,
                            "$brand;$item",
                            brandOBJECT.get(item).toString().toDouble()
                        )
                    )
                    id++
                }
            }
            PriceRepository.loadPrices()
        }
        /* test */
        convertPriceList()
    }
}