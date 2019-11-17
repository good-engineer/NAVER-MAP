package com.naver.navermap

import android.content.res.AssetManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import org.json.JSONArray
import java.io.InputStream
import kotlin.math.*

//TODO
/*implement viterbi algorithm
1. set current location as initial state and find connected roads
2. get a sequence of location
3. calculate emission probability
4. find the best path
5. update state

*/

data class Coordination(
    val x: Double, val y: Double, val z: Double
)

fun dot(coord1: Coordination, coord2: Coordination): Double {
    return coord1.x * coord2.x + coord1.y * coord2.y + coord1.z * coord2.z
}

fun plusCoord(coord1: Coordination, coord2: Coordination): Coordination {
    return Coordination(coord1.x + coord2.x, coord1.y + coord2.y, coord1.z + coord2.z)
}

fun sizeCoord(coord: Coordination): Double {
    return sqrt(dot(coord, coord))
}

fun cardProject(
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

fun pointToLineProject(road: RoadState, location: LatLng): LatLng {
    val southWest: LatLng =
        LatLng(
            min(road.startPoint.latitude, road.endPoint.latitude),
            min(road.startPoint.longitude, road.endPoint.longitude)
        )
    val northEast: LatLng =
        LatLng(
            max(road.startPoint.latitude, road.endPoint.latitude),
            max(road.startPoint.longitude, road.endPoint.longitude)
        )
    val bound: LatLngBounds = LatLngBounds(southWest, northEast)
    val point = cartToLatLng(
        cardProject(
            latLngToCart(road.startPoint),
            latLngToCart(road.endPoint),
            latLngToCart(location)
        )
    )

    if (bound.contains(point))
        return point
    else {
        return if (road.startPoint.distanceTo(point) < road.endPoint.distanceTo(point)) road.startPoint else road.endPoint
    }
}

class Viterbi(jsonString: String) {
    var prevLocation: LatLng? = null
    var currLocation: LatLng? = null
    var prevStates: List<Pair<RoadState, Double>>? = null
    var currStates: List<Pair<RoadState, Double>>? = null
    lateinit var roadMap: Map<Int, RoadState>

    companion object {
        private const val BETA = 6.0
        private const val SIGMAZ = 10.0
    }

    init {
        val tmpMap: MutableMap<Int, RoadState> = mutableMapOf()
        val jArray = JSONArray(jsonString)
        for (i in 0 until jArray.length()) {
            val road = jArray.getJSONObject(i)
            val seqNum = road.getInt("sequence")
            val distance = road.getInt("distance")
            val pathPoints = road.getJSONArray("pathPoints")
            var lastlocation: LatLng = LatLng(0.0, 0.0)
            for (j in 0 until pathPoints.length()) {
                val pathPoint = pathPoints.getJSONArray(j)
                val location = LatLng(pathPoint.optDouble(1), pathPoint.optDouble(0))
                if (j != 0) {
                    tmpMap.put(
                        seqNum * 10 + (j - 1),
                        RoadState(lastlocation, location, lastlocation.distanceTo(location))
                    )
                }
                lastlocation = location
            }
        }
        roadMap = tmpMap
    }

    fun getEmissionProb(road: RoadState, location: LatLng): Double {
        val C = 1 / (sqrt(2 * PI) * SIGMAZ)
        val roadLocation = getRoadLocation(road, location)
        val distance = location.distanceTo(roadLocation)
        return C * exp(-0.5 * (distance / SIGMAZ) * (distance / SIGMAZ))
    }

    fun getTransitionProb(from: RoadState, to: RoadState): Double {
        TODO("get transition probability form road to to road")
    }

    //get candidate location in 200m
    fun getCandidate(location: LatLng): List<RoadState> {
        val res: MutableList<RoadState> = mutableListOf()
        for ((seq, road) in roadMap) {
            if (location.distanceTo(road.startPoint) < 200 ||
                location.distanceTo(road.endPoint) < 200
            )
                res.add(road)
        }
        return res
    }

    fun getRoadLocation(road: RoadState, location: LatLng): LatLng {
        return pointToLineProject(road, location)
    }

    fun getMapMatchingLocation(location: LatLng): LatLng {
        prevStates = currStates
        prevLocation = currLocation
        val currCandidates = getCandidate(location)
        var currRoad: RoadState? = null
        currStates = currCandidates.map { currCand ->
            prevStates?.let { prevStates ->
                var maxProb = 0.0
                for ((prevRoad, prevProb) in prevStates) {
                    val tmpProb =
                        prevProb * getTransitionProb(prevRoad, currCand) * getEmissionProb(
                            currCand,
                            location
                        )
                    if (maxProb < tmpProb) {
                        maxProb = tmpProb
                    }
                }
                Pair(currCand, maxProb)
            } ?: Pair(currCand, 0.0)
        }
        currLocation = location
        currStates?.let { currStates ->
            var maxProb = 0.0
            for ((road, prob) in currStates) {
                if (maxProb < prob) {
                    maxProb = prob
                    currRoad = road
                }
            }
        }
        return currRoad?.let { getRoadLocation(it, location) } ?: LatLng(0.0, 0.0)

    }
}
