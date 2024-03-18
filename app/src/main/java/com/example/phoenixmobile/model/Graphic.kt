package com.example.phoenixmobile.model
data class Graphic(
    // the brand of the phone
    var mark : String,
    var model : String,
    // possible states and price arrays
    var prices : ArrayList<States>,
)
