package com.example.phoenixmobile.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReportDto::class, PriceDto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun priceDao(): PriceDao
}