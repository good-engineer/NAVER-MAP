package com.naver.navermap

import android.content.Context
import com.naver.maps.geometry.LatLng
import com.naver.navermap.data.Coordination
import com.naver.navermap.data.RetroResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetroFitAPI {
    private lateinit var listener: (List<Coordination>) -> Unit
    private lateinit var context: Context
    val service: RetrofitService

    companion object {
        @Volatile
        private var INSTANCE: RetroFitAPI? = null

        fun getInstance(): RetroFitAPI {
            return INSTANCE ?: synchronized(this) {
                RetroFitAPI().also { INSTANCE = it }
            }
        }
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.osrm_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(RetrofitService::class.java)
    }

    fun setListener(listener: (List<Coordination>) -> Unit) {
        this.listener = listener
    }

    fun setContext(context: Context){
        this.context = context
    }

    fun getRetroFitClient(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ) {
        service.requestRoute(
            coordinate = "${startLong},${startLat};${endLong},${endLat}",
            overview = false
        ).enqueue(object :
            Callback<Route> {
            override fun onFailure(call: Call<Route>, t: Throwable) {
                listener(listOf(Coordination(RetroResult.NoInternetError(t), LatLng(0.0, 0.0))))
            }

            override fun onResponse(call: Call<Route>, response: Response<Route>) {
                if (response.isSuccessful) {
                    listener(response.body()?.let {
                        it.routes[0].legs[0].steps.map { it ->
                            Coordination(
                                RetroResult.Success(context.getString(R.string.retrofit_success)),
                                LatLng(it.maneuver.location[1], it.maneuver.location[0])
                            )
                        }
                    } ?: emptyList())
                } else {
                    listener(
                        listOf(
                            Coordination(
                                RetroResult.NoResponseError(null),
                                LatLng(0.0, 0.0)
                            )
                        )
                    )
                }

            }
        })
    }

}
