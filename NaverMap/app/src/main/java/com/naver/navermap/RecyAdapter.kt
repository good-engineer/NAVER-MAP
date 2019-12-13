package com.naver.navermap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.naver.navermap.data.RouteItem

class RecyAdapter(val context: Context, val routeList: List<RouteItem>) :
    RecyclerView.Adapter<RecyAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyview_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return routeList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(routeList[position], context)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val startPoint = itemView.findViewById<TextView>(R.id.start_text)
        val distance = itemView.findViewById<TextView>(R.id.dist_text)
        val direction = itemView.findViewById<TextView>(R.id.direct_text)

        fun bind(route: RouteItem, context: Context) {
            startPoint.text = route.source.toString()
            distance.text = route.distance.toString()
            direction.text = route.direction.toString()
        }
    }
}
