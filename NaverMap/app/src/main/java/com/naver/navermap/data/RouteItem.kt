package com.naver.navermap.data

import com.naver.maps.geometry.LatLng

enum class Direction { FRONT, RIGHT, LEFT, BACK, NIL }
data class RouteItem(val source: LatLng, val distance: Double, val direction: Direction)
