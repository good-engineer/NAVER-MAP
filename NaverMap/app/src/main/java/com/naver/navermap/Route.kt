package com.naver.navermap

data class Route(
    val routes : List<RoutesData>,
    val waypoints : List<WaypointsData>,
    val code : String
)
data class RoutesData(
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
data class WaypointsData(
    val hint : String,
    val distance : Double,
    val name : String,
    val location : List<Double>
)