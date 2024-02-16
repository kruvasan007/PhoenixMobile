package com.example.phoenixmobile.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReportDto::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}