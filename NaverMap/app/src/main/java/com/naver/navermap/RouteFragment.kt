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

class RouteFragment private constructor(val routeList: List<RouteItem>) : Fragment() {

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
        fun newInstance(routeList: List<RouteItem>): RouteFragment {
            return RouteFragment(routeList)
        }
    }
}
