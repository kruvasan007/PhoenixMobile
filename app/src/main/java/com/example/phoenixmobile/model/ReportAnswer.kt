package com.example.phoenixmobile.model

data class ReportAnswer (
    // data model to receive from the server
    var mark : String,
    var model : String,
    var condition : String,
    var price : Float,
    var url : String,
    var report_id : Long,
)