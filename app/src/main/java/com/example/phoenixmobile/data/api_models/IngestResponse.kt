package com.example.phoenixmobile.data.api_models

data class TripletDto(val subject: String, val relation: String, val `object`: String)

data class IngestResponse(val message: String?, val triplets: List<TripletDto>?)

