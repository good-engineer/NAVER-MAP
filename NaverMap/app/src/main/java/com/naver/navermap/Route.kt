package com.naver.navermap

data class Route(
    val routes: List<RoutesData>,
    val waypoints: List<WaypointsData>,
    val code: String
)

data class RoutesData(
    val legs: List<Leg>,
    val weight_name: String,
    val weight: Double,
    val duration: Double,
    val distance: Double
)

data class Leg(
    val summary: String,
    val weight: Double,
    val duration: Double,
    val steps: List<Step>
)

data class Step(
    val distance: Double,
    val maneuver: StepManeuver
)

data class StepManeuver(
    val bearing_after: Double,
    val bearing_before: Double,
    val location: List<Double>
)

data class WaypointsData(
    val hint: String,
    val distance: Double,
    val name: String,
    val location: List<Double>
)
