package com.naver.navermap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.navermap.data.*
import kotlinx.android.synthetic.main.fragment_route.*

class RouteFragment private constructor(pointList: List<LatLng>) : Fragment() {
    private val routeList: List<RouteItem>

    init {
        val tmpList: MutableList<RouteItem> = mutableListOf()
        fun getDirection(start: LatLng, middle: LatLng, end: LatLng): Direction {
            val cos =
                vecCos(start, middle, middle, end)
            val direct = dot(outerProduct(start, middle, middle, end), latLngToCart(middle))
            if (cos > 0.5) return Direction.FRONT
            else if (cos < -0.5) return Direction.BACK
            else {
                if (direct > 0) return Direction.LEFT
                else return Direction.RIGHT
            }
        }
        for (i in 0 until pointList.size) {
            //when last position (last position, 0, no movement)
            if (i == pointList.size - 1) tmpList.add(RouteItem(pointList[i], 0.0, Direction.NIL))
            else tmpList.add(
                RouteItem(
                    pointList[i],
                    pointList[i].distanceTo(pointList[i + 1]),
                    if (i == 0) Direction.FRONT
                    else {
                        getDirection(pointList[i - 1], pointList[i], pointList[i + 1])
                    }
                )
            )
        }
        routeList = tmpList
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.apply {
            activity?.let {
                adapter = RecyAdapter(it, routeList)
            } ?: return@onViewCreated
            layoutManager = LinearLayoutManager(activity)
        }
        val source = view.findViewById(R.id.source_text) as TextView
        val sourceText =
            "%.6f, ".format(routeList[0].source.latitude) + "%.6f".format(routeList[0].source.longitude)
        source.text = sourceText
        val dest = view.findViewById(R.id.dest_text) as TextView
        val destText =
            "%.6f, ".format(routeList[routeList.size - 1].source.latitude) + "%.6f".format(routeList[routeList.size - 1].source.longitude)
        dest.text = destText
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route, container, false)
    }

    companion object {
        fun newInstance(pointList: List<LatLng>): RouteFragment {
            return RouteFragment(pointList)
        }
    }
}
