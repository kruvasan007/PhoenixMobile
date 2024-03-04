package com.example.phoenixmobile.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pricetable")
data class PriceDto(
    @PrimaryKey(autoGenerate = true)
    val id: Int ?= 0,
    @ColumnInfo(name = "model") var model: String,
    @ColumnInfo(name = "price") var price: Double
)