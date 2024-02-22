package com.example.phoenixmobile.database


import androidx.room.Dao
import androidx.room.Insert

@Dao
interface ReportDao {

    @Insert(entity = ReportDto::class)
    fun insertReport(report: ReportDto)

    /*@Query("SELECT * FROM report")
    fun getAll(): List<ReportDto>*/

    /*@Delete
    fun delete(reportDto: ReportDto)*/
}