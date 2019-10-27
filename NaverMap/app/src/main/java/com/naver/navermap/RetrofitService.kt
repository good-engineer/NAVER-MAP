package com.naver.navermap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService{
    //http://router.project-osrm.org/
    @GET("/route/v1/foot/{coordinate}")
    fun requestRoute(
        @Path("coordinate") coordinate : String
        /*@Query("alternative") alternative : Boolean = false,
        @Query("steps") steps : Boolean = false,
        @Query("annotation") annotation : Boolean = false,
        @Query("geometries") geometries : String = "polyline",
        @Query("overview") overview : String = "simplified",
        @Query("continue_straight") continue_straight : String = "default",
        @Query("waypoints") waypoints : String = "default"*/
    ) : Call<Route>
}