package com.naver.navermap

import android.location.Location

class RoadState {
    //TODO : implement Road state
    private var startPoint: Location? = null
    private var endPoint: Location? =null
    private var length: Int?=0
    //more variable?

    fun RoadState(info:String? ){
    //get an string containing the road info and set the variables
    }
    fun setRoadState(start:Location?, end:Location? ){
        //set road state
    }
    fun getRoadState(){
        //get road info

    }
    fun getStartPoint(){

    }
    fun getEndPoint(){

    }
    fun getlength(){

    }

}


class hmm {
    private var currentState: RoadState? = null
    private val roadSequence: MutableList<RoadState> = arrayListOf()
    private val transitionProb: MutableList<Int> = arrayListOf()

    init {
        //request last 2 location
        //set initial curr state by parsing jason data
    }

    fun setRoadSequence(location: Location?){
        //set a road sequence by parsing jason data in appreciate to current road or location ?
    }

    fun TransitionProb(location1: Location?,location2: Location?){
        //get 2 last location and calculate the corresponding prob for every roadstate in list
    }

    fun setCurrentState(location1:Location?,location2:Location? ) {
        //set curr state accoriding to transition probability

    }

    fun getCurrentState(){

    }
    fun getRoadSequence(){

    }
    fun getTransitionProb(){
        
    }





}




