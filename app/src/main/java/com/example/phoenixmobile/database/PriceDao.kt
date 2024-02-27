package com.example.phoenixmobile.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PriceDao {
    @Insert(entity = PriceDto::class)
    fun insertModel(priceDto: PriceDto)
}