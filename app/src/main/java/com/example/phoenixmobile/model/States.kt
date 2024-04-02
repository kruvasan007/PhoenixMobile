package com.example.phoenixmobile.model

data class States(
    // string name of state phone
    var state: String,
    // the unit of data in the price table for a specific device condition
    var priceList: ArrayList<Float>
)