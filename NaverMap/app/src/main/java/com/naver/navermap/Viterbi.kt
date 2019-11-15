package com.naver.navermap

import com.naver.maps.geometry.LatLng

//TODO
/*implement viterbi algorithm
1. set current location as initial state and find connected roads
2. get a sequence of location
3. calculate emission probability
4. find the best path
5. update state

*/

class Viterbi {
    companion object {
        private const val BETA = 6.0
        private const val SIGMAZ = 10.0
    }

    var prevLocation: LatLng? = null
    var currLocation: LatLng? = null
    var prevStates: List<Pair<RoadState, Double>>? = null
    var currStates: List<Pair<RoadState, Double>>? = null
    fun getEmissionProb(road: RoadState, location: LatLng): Double {
        TODO("get emission probability")
    }

    fun getTransitionProb(from: RoadState, to: RoadState): Double {
        TODO("get transition probability form road to to road")
    }

    fun getCandidate(location: LatLng): List<RoadState>? {
        TODO("get candidate road in 200m")
    }

    fun getRoadLocation(road: RoadState, location: LatLng): LatLng {
        TODO("location projected to road")
    }

    fun getMapMatchingLocation(location: LatLng): LatLng {
        prevStates = currStates
        prevLocation = currLocation
        val currCandidates = getCandidate(location)
        var currRoad: RoadState? = null
        currStates = currCandidates?.map { currCand ->
            prevStates?.let { prevStates ->
                var maxProb = 0.0
                for ((prevRoad, prevProb) in prevStates) {
                    val tmpProb =
                        prevProb * getTransitionProb(prevRoad, currCand) * getEmissionProb(currCand, location)
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
