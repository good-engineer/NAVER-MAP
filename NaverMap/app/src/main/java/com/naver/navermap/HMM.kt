package com.naver.navermap

interface RoadState {
    //TODO : implement proper interface
    fun setCurrentState(state: RoadState)

    fun addDirectedRoad(road: RoadState)
    fun addOnelinkRoad(road: RoadState)

}


class RoadStateContext {
    private var currentState: RoadState? = null
    private var length: Int? = 0
    private val directRoadList: MutableList<RoadState> = arrayListOf()
    private val oneLinkRoadList: MutableList<RoadState> = arrayListOf()

    init {
        currentState = InitialState()
    }

    fun setLength(l: Int) {
        length = l
    }

    fun setCurrentState(state: RoadState) {
        currentState = state
    }

    fun addDirectedRoad(road: RoadState) {
        directRoadList.add(road)
    }

    fun addOnelinkRoad(road: RoadState) {
        oneLinkRoadList.add(road)
    }


}

class InitialState : RoadState {

    //TODO: set initial state accoriding to last known location
    // TODO: find the connected roads and add

    override fun setCurrentState(state: RoadState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addDirectedRoad(road: RoadState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addOnelinkRoad(road: RoadState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}


