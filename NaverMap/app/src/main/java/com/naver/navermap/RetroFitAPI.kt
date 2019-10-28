package com.naver.navermap

import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetroFitAPI : Fragment() {
    private lateinit var listener : (List<coordination>) -> Unit
    val service : RetrofitService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(RetrofitService::class.java)
    }
    data class coordination(val latitude : Double, val longitude : Double)
    private fun waypoint2coordination(route : List<WaypointsData>?) : List<coordination>{
        return route?.map{it -> coordination(it.location[1], it.location[0])} ?: emptyList()
    }
    fun setListener(listener : (List<coordination>) -> Unit){
        this.listener = listener
    }
    public fun getRetroFitClient(startLat : Double, startLong : Double, endLat : Double, endLong : Double) {
        service.requestRoute(coordinate = "${startLong},${startLat};${endLong},${endLat}", overview = false).enqueue(object :
            Callback<Route> {
            override fun onFailure(call: Call<Route>, t: Throwable) {
                Toast.makeText(
                    activity, "Check Internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
            override fun onResponse(call: Call<Route>, response: Response<Route>) {
                if(response.isSuccessful) {
                    /*Toast.makeText(
                        activity, "Response",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    listener(waypoint2coordination(response.body()?.waypoints))
                }else{
                    Toast.makeText(
                        activity, "Response error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}