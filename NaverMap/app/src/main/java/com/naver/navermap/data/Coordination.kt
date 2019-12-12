package com.naver.navermap.data

import com.naver.maps.geometry.LatLng
import kotlin.math.*

data class Coordination(
    val x: Double, val y: Double, val z: Double
)

fun dot(coord1: Coordination, coord2: Coordination): Double {
    return coord1.x * coord2.x + coord1.y * coord2.y + coord1.z * coord2.z
}

fun plusCoord(coord1: Coordination, coord2: Coordination): Coordination {
    return Coordination(coord1.x + coord2.x, coord1.y + coord2.y, coord1.z + coord2.z)
}

fun subCoord(coord1: Coordination, coord2: Coordination): Coordination {
    return Coordination(coord1.x - coord2.x, coord1.y - coord2.y, coord1.z - coord2.z)
}

fun sizeCoord(coord: Coordination): Double {
    return sqrt(dot(coord, coord))
}

fun cartProject(
    endPoint1: Coordination,
    endPoint2: Coordination,
    location: Coordination
): Coordination {
    val R = 6371000
    val line = Coordination(
        endPoint2.x - endPoint1.x,
        endPoint2.y - endPoint1.y,
        endPoint2.z - endPoint1.z
    )
    val inner =
        dot(
            Coordination(
                location.x - endPoint1.x,
                location.y - endPoint1.y,
                location.z - endPoint1.z
            ), line
        )
    val lineSize = dot(line, line)
    val proj = plusCoord(
        endPoint1,
        Coordination(
            line.x * inner / lineSize,
            line.y * inner / lineSize,
            line.z * inner / lineSize
        )
    )
    val projSize = sizeCoord(proj)
    return Coordination(proj.x * R / projSize, proj.y * R / projSize, proj.z * R / projSize)
}

fun latLngToCart(location: LatLng): Coordination {
    val R = 6371000
    val x = R * cos(location.latitude * (2 * PI / 360)) * cos(location.longitude * (2 * PI / 360))
    val y = R * cos(location.latitude * (2 * PI / 360)) * sin(location.longitude * (2 * PI / 360))
    val z = R * sin(location.latitude * (2 * PI / 360))
    return Coordination(x, y, z)
}

fun cartToLatLng(coord: Coordination): LatLng {
    val R = 6371000
    val lat = asin(coord.z / R) * (360 / (2 * PI))
    val lon = atan2(coord.y, coord.x) * (360 / (2 * PI))
    return (LatLng(lat, lon))
}

fun outerProduct(start: Coordination, end: Coordination): Coordination {
    return Coordination(start.y * end.z - start.z * end.y, start.z * end.x - start.x * end.z, start.x * end.y - start.y * end.x)
}

fun outerProduct(start1: LatLng, start2: LatLng, end1: LatLng, end2: LatLng): Coordination {
    val start = subCoord(latLngToCart(start2), latLngToCart(start1))
    val end = subCoord(latLngToCart(end2), latLngToCart(end1))
    return Coordination(start.y * end.z - start.z * end.y, start.z * end.x - start.x * end.z, start.x * end.y - start.y * end.x)
}

fun vecCos(start1: LatLng, start2: LatLng, end1: LatLng, end2: LatLng): Double {
    val start = subCoord(latLngToCart(start2), latLngToCart(start1))
    val end = subCoord(latLngToCart(end2), latLngToCart(end1))
    val inner = dot(start, end)
    return inner / (sizeCoord(start) * sizeCoord(end))
}
