package com.example.phoenixmobile.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report")
data class ReportDto(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "report") var report: String ?= null,
)
