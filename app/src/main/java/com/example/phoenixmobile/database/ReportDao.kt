package com.example.phoenixmobile.database


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.phoenixmobile.model.Report

@Dao
interface ReportDao {

    @Insert(entity = ReportDto::class)
    fun insertReport(report: ReportDto)

    /*@Query("SELECT * FROM report")
    fun getAll(): List<ReportDto>*/

    /*@Delete
    fun delete(reportDto: ReportDto)*/
}