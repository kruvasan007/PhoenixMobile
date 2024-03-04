package com.example.phoenixmobile.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PriceDto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
}