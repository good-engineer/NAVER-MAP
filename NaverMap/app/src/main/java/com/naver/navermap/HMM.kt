package com.naver.navermap

import android.location.Location
import com.naver.maps.geometry.LatLng

data class RoadState(
    val startPoint: LatLng,
    val endPoint: LatLng,
    val distance: Double
)


class hmm {
    private var currentState: RoadState? = null
    private val roadSequence: MutableList<RoadState> = arrayListOf()
    private val transitionProb: MutableList<Int> = arrayListOf()

    init {
        //request last 2 location
        //set initial curr state by parsing jason data
    }

    fun setRoadSequence(location: Location?) {
        //set a road sequence by parsing jason data in appreciate to current road or location ?
    }

    fun TransitionProb(location1: Location?, location2: Location?) {
        //get 2 last location and calculate the corresponding prob for every roadstate in list
    }

    fun setCurrentState(location1: Location?, location2: Location?) {
        //set curr state accoriding to transition probability

    }

    fun getCurrentState() {

    }

    fun getRoadSequence() {

    }

    fun getTransitionProb() {

    }


}




