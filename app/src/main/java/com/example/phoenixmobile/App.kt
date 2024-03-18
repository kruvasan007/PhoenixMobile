package com.example.phoenixmobile

import android.app.Application
import androidx.room.Room
import com.example.phoenixmobile.database.AppDatabase

class App : Application() {
    companion object {
        var reportDatabase: AppDatabase? = null
        fun getDatabase(): AppDatabase? {
            return reportDatabase
        }
    }

    override fun onCreate() {
        super.onCreate()
        reportDatabase = Room
            .databaseBuilder(applicationContext, AppDatabase::class.java, "DB")
            .allowMainThreadQueries()
            .build()
    }

}