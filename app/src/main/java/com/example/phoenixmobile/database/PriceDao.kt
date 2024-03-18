package com.example.phoenixmobile.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PriceDao {
    //create all the necessary methods to access the database
    @Query("SELECT * FROM pricetable")
    fun getAll(): List<PriceDto>

    @Query("DELETE FROM pricetable")
    fun deleteAll()

    @Insert(entity = PriceDto::class)
    fun insertModel(priceDto: PriceDto)
}