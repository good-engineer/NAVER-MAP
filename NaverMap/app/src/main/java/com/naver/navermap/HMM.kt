package com.naver.navermap

import com.naver.maps.geometry.LatLng

data class RoadState(
    val startPoint: LatLng,
    val endPoint: LatLng,
    val distance: Double
)

