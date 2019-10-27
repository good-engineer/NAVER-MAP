package com.naver.navermap

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetroFitAPI : ViewModel() {
    private lateinit var service : RetrofitService
    var routeList = MutableLiveData<ArrayList<Pair<Double, Double>>>()
    init {
        setRetroFit()
    }
    private fun setRetroFit(){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        service = retrofit.create(RetrofitService::class.java)
    }
    private fun route2list(route : List<WaypointsData>?) : ArrayList<Pair<Double, Double>>{
        val list = ArrayList<Pair<Double, Double>>()
        route?.forEach {list.add(Pair(it.location[1], it.location[0]))}
        return list
    }
    public fun getRetroFitClient(startLat : Double, startLong : Double, endLat : Double, endLong : Double){
        Log.d("TAG", "request 시작")
        val response = service.requestRoute(coordinate = "${startLong},${startLat};${endLong},${endLat}", overview = false).enqueue(object :
            Callback<Route> {
            override fun onFailure(call: Call<Route>, t: Throwable) {
            }
            override fun onResponse(call: Call<Route>, response: Response<Route>) {
                if(response.isSuccessful) {
                    Log.d("TAG", "시작!")
                    Log.d("TAG", "${response.code()}")
                    routeList.setValue(route2list(response.body()?.waypoints))
                    Log.d("TAG", "보내기 완료")
                }
            }
        })
        Log.d("TAG", "request 끝")
    }
}