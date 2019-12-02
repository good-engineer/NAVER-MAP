package com.naver.navermap

import android.location.Location
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import org.json.JSONArray
import java.sql.Time
import java.util.*
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

data class TimeStep(
    val location: LatLng,
    val candidates: List<RoadState>,
    val states: List<Pair<RoadState, Double>>
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
    val point = cartToLatLng(
        cardProject(
            latLngToCart(road.startPoint),
            latLngToCart(road.endPoint),
            latLngToCart(location)
        )
    )
    return point
}

class Viterbi(jsonString: String) {
    private var prevStep: TimeStep? = null
    private var currStep: TimeStep? = null
    private var currSpeed: Float = 0.0f
    private var currTime: Long = 0
    private var counter: Int = 0
    private val roadMap: Map<Int, RoadState>

    companion object {
        private const val INF: Int = 0x3F3F3F3F
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
            var lastLocation = LatLng(0.0, 0.0)
            for (j in 0 until pathPoints.length()) {
                val pathPoint = pathPoints.getJSONArray(j)
                val location = LatLng(pathPoint.optDouble(1), pathPoint.optDouble(0))
                if (j != 0) {
                    tmpMap[seqNum * 100 + (j - 1)] =
                        RoadState(lastLocation, location, lastLocation.distanceTo(location))
                }
                lastLocation = location
            }
        }
        roadMap = tmpMap
    }

    private fun getEmissionProb(road: RoadState, location: LatLng): Double {
        val C = 1 / (sqrt(2 * PI) * SIGMAZ)
        val roadLocation = getRoadLocation(road, location)
        val distance = location.distanceTo(roadLocation)
        return C * exp(-0.5 * (distance / SIGMAZ) * (distance / SIGMAZ))
    }

    private fun getTransitionProb(from: RoadState, to: RoadState): Double {

        val prevRoadLocation = getRoadLocation(from, prevStep!!.location)
        val currRoadLocation = getRoadLocation(to, currStep!!.location)
        val dist: MutableMap<LatLng, Int> = mutableMapOf()

        fun makeAdj(roadList: List<RoadState>): Map<LatLng, MutableList<Pair<LatLng, Int>>> {
            val adj = mutableMapOf<LatLng, MutableList<Pair<LatLng, Int>>>()
            for (road in roadList) {
                adj[road.startPoint]?.add(Pair(road.endPoint, road.distance.roundToInt()))
                    ?: adj.put(
                        road.startPoint,
                        mutableListOf(Pair(road.endPoint, road.distance.roundToInt()))
                    )
                adj[road.endPoint]?.add(Pair(road.startPoint, road.distance.roundToInt()))
                    ?: adj.put(
                        road.endPoint,
                        mutableListOf(Pair(road.startPoint, road.distance.roundToInt()))
                    )
                dist.put(road.startPoint, INF)
                dist.put(road.endPoint, INF)
            }
            return adj
        }

        fun dijkstra(start: LatLng, adj: Map<LatLng, MutableList<Pair<LatLng, Int>>>) {
            val queue = PriorityQueue<LatLng>(adj.size, { n1, n2 -> dist[n1]!! - dist[n2]!! })
            queue.offer(start)
            while (!queue.isEmpty()) {
                val node = queue.poll()
                for ((next, d) in adj[node]!!) {
                    val weight = dist[node]!! + d
                    if (dist[next] ?: 0 > weight) {
                        dist[next] = weight
                        queue.offer(next)
                    }
                }
            }
        }

        val roadList: List<RoadState> = prevStep!!.candidates.toMutableList().apply {
            if (from == to) {
                addAll(currStep!!.candidates)
                remove(from)
                if (prevRoadLocation != currRoadLocation) add(
                    RoadState(
                        prevRoadLocation,
                        currRoadLocation,
                        prevRoadLocation.distanceTo(currRoadLocation)
                    )
                )
                if (prevRoadLocation.distanceTo(from.startPoint) > currRoadLocation.distanceTo(from.startPoint)) {
                    if (from.startPoint != currRoadLocation) add(
                        RoadState(
                            from.startPoint,
                            currRoadLocation,
                            from.startPoint.distanceTo(currRoadLocation)
                        )
                    )
                    if (from.endPoint != prevRoadLocation) add(
                        RoadState(
                            from.endPoint,
                            prevRoadLocation,
                            from.endPoint.distanceTo(prevRoadLocation)
                        )
                    )
                } else {
                    if (from.startPoint != prevRoadLocation) add(
                        RoadState(
                            from.startPoint,
                            prevRoadLocation,
                            from.startPoint.distanceTo(prevRoadLocation)
                        )
                    )
                    if (from.endPoint != currRoadLocation) add(
                        RoadState(
                            from.endPoint,
                            currRoadLocation,
                            from.endPoint.distanceTo(currRoadLocation)
                        )
                    )
                }
            } else {
                addAll(currStep!!.candidates)
                remove(from)
                remove(to)
                if (from.startPoint != prevRoadLocation) {
                    add(
                        RoadState(
                            from.startPoint,
                            prevRoadLocation,
                            from.startPoint.distanceTo(prevRoadLocation)
                        )
                    )
                }
                if (from.endPoint != prevRoadLocation) {
                    add(
                        RoadState(
                            from.endPoint,
                            prevRoadLocation,
                            from.endPoint.distanceTo(prevRoadLocation)
                        )
                    )
                }
                if (from.startPoint != currRoadLocation) {
                    add(
                        RoadState(
                            to.startPoint,
                            currRoadLocation,
                            to.startPoint.distanceTo(currRoadLocation)
                        )
                    )
                }
                if (from.endPoint != currRoadLocation) {
                    add(
                        RoadState(
                            to.endPoint,
                            currRoadLocation,
                            to.endPoint.distanceTo(currRoadLocation)
                        )
                    )
                }
            }
        }.toSet().toList() //delete duplicate road
        val adj = makeAdj(roadList)
        dist.set(prevRoadLocation, 0)
        dijkstra(prevRoadLocation, adj)
        val minDist = dist[currRoadLocation]
        val greatCircle = prevStep!!.location.distanceTo(currStep!!.location)
        val prob =
            if (minDist == INF) 0.0 else 1.0 / BETA * exp(-abs(minDist!!.toDouble() - greatCircle) / BETA)
        return prob
    }

    //get candidate location in 200m
    private fun getCandidate(location: LatLng): List<RoadState> {
        val res: MutableList<RoadState> = mutableListOf()
        for ((_, road) in roadMap) {
            if (location.distanceTo(road.startPoint) < 200 ||
                location.distanceTo(road.endPoint) < 200
            )
                res.add(road)
        }
        return res
    }

    private fun getRoadLocation(road: RoadState, location: LatLng): LatLng {
        val point = pointToLineProject(road, location)
        val southWest =
            LatLng(
                min(road.startPoint.latitude, road.endPoint.latitude),
                min(road.startPoint.longitude, road.endPoint.longitude)
            )
        val northEast =
            LatLng(
                max(road.startPoint.latitude, road.endPoint.latitude),
                max(road.startPoint.longitude, road.endPoint.longitude)
            )
        val bound = LatLngBounds(southWest, northEast)
        return if (bound.contains(point))
            point
        else {
            if (road.startPoint.distanceTo(point) < road.endPoint.distanceTo(point)) road.startPoint else road.endPoint
        }
    }

    private fun isNotValid(location: Location): Boolean {

        // position of location in LatLng type
        val pos = LatLng(location.latitude, location.longitude)
        // t : time difference
        val t : Double = 1.0
        // in other case: val t: Double = (location.time - currTime).toDouble() / 1000.0
        // a : acceleration
        val a: Double = ((location.speed - currSpeed) / t)
        // x : predicted distance of new location to old location
        val x: Double = (0.5 * a * t.pow(2)) + (t * currSpeed)

        // d : distance from currlocation to new coming location
        val d: Double? = currStep!!.location?.let { pos.distanceTo(it) }

        if (d != null) {
            //according to new location's accuracy and predicted distance x
            // if the new location is too far so the location is not valid
            // set a counter
            if (d > ((2 * location.accuracy) + x)) {

                return false
            }
        }

        return true
    }

    fun getMapMatchingLocation(inputLocation: Location): LatLng {
        // counter :the number of false location

        var currRoad: RoadState? = null
        val location = LatLng(inputLocation.latitude, inputLocation.longitude)

        if (inputLocation.hasAccuracy() ){ //&& inputLocation.hasSpeed()) {
            //check if location in valid
            if (isNotValid(location = inputLocation)){
                counter +=1
                }

            if (counter != 0) {
                if (counter < 3) {
                    return currRoad?.let { getRoadLocation(it, currStep!!.location) }
                        ?: currStep!!.location
                } else {
                    prevStep = null
                    counter = 0
                }
            }
        } else{
            return currRoad?.let { getRoadLocation(it, currStep!!.location) }
                ?: currStep!!.location
        }

        currSpeed = inputLocation.speed
        currTime = inputLocation.time
        prevStep = currStep
        currStep = TimeStep(location, getCandidate(location), listOf())

        // when empty candidate
        if (currStep?.candidates.isNullOrEmpty()) return location
        var currStates = currStep!!.candidates.map { currCand ->
            if (prevStep == null) Pair(currCand, getEmissionProb(currCand, location))
            else {
                prevStep!!.states.let { prevStates ->
                    var maxProb = 0.0
                    for ((prevRoad, prevProb) in prevStates) {
                        val tmpProb =
                            prevProb * getTransitionProb(prevRoad, currCand) * getEmissionProb(
                                currCand,
                                location
                            )
                        if (maxProb < tmpProb)
                            maxProb = tmpProb
                    }
                    Pair(currCand, maxProb)
                }
            }
        }

        var probSum = 0.0
        for ((_, prob) in currStates) {
            probSum = probSum + prob
        }

        // current location is too far from previous location
        if (probSum == 0.0) {
            currStates = currStates.map { (currCand, _) ->
                Pair(
                    currCand,
                    getEmissionProb(currCand, location)
                )
            }
            for ((_, prob) in currStates) {
                probSum = probSum + prob
            }
        }

        //normalization & remove prob < 2.0%
        currStates =
            currStates.map { Pair(it.first, it.second / probSum) }.filter { it.second > 0.02 }

        var maxProb = 0.0
        for ((road, prob) in currStates) {
            if (maxProb < prob) {
                maxProb = prob
                currRoad = road
            }
        }

        currStep = TimeStep(location, currStep.let { it!!.candidates }, currStates)

        return currRoad?.let { getRoadLocation(it, location) } ?: location
    }
}
