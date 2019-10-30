package com.naver.navermap

interface RoadState {
    //TODO : implement proper interface
    fun setCurrentState(state:RoadState)
    fun addDirectedRoad (road: RoadState)
    fun addOnelinkRoad(road: RoadState)

}

class RoadStateContext{
    private var currentState: RoadState? = null
    private var length: Int? = 0
    private val directRoadList : MutableList <RoadState> = arrayListOf()
    private val oneLinkRoadList : MutableList <RoadState> = arrayListOf()

    init {
        //TODO: set initial state accoriding to last known location
        // TODO: find the connected roads and add

    }

    fun setLength(l:Int){
        length=l
    }

    fun setCurrentState (state: RoadState){
        currentState = state
    }

    fun addDirectedRoad (road: RoadState){
        directRoadList.add(road)
    }

    fun addOnelinkRoad(road: RoadState){
        oneLinkRoadList.add(road)
    }

    
}