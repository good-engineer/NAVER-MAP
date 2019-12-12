package com.naver.navermap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.navermap.data.Direction
import com.naver.navermap.data.RouteItem
import kotlinx.android.synthetic.main.fragment_route.*

class RouteFragment(pointList : List<LatLng>) : Fragment() {
    //var routeList: List<RouteItem> =
    //   listOf(RouteItem(LatLng(37.4603, 126.95), 10.0, Direction.FRONT))
    val routeList: List<RouteItem>

    init {
        val tmpList: MutableList<RouteItem> = mutableListOf()
        for (i in 0 until pointList.size) {
            if(i == pointList.size - 1) tmpList.add(RouteItem(pointList[i], 0.0, Direction.FRONT))
            else tmpList.add(RouteItem(pointList[i], pointList[i].distanceTo(pointList[i + 1]), Direction.FRONT))
        }
        routeList = tmpList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = RecyAdapter(activity!!, routeList)
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route, container, false)
    }

    companion object {
        fun newinstance(pointList : List<LatLng>): RouteFragment {
            return RouteFragment(pointList)
        }
    }
}
