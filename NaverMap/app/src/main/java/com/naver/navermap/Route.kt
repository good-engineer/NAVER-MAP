package com.naver.navermap

data class Route(
    val routes : List<RoutesDeta>,
    val waypoints : List<WaypointsDeta>,
    val code : String
)
data class RoutesDeta(
    val legs : List<LegsData>,
    val weight_name : String,
    val weight : Double,
    val duration : Double,
    val distance : Double
)
data class LegsData(
    val distance : Double,
    val duration: Double
)
data class WaypointsDeta(
    val hint : String,
    val distance : Double,
    val name : String,
    val location : List<Double>
)