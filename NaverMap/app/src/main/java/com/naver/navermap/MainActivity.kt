package com.naver.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.lang.RuntimeException


const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var naverMap: NaverMap
    private lateinit var service : RetrofitService

    private fun setRetroFit(){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://router.project-osrm.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        service = retrofit.create(RetrofitService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //map fragment manager
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        //search fragment
        fm.beginTransaction().add(R.id.container, SearchFragment()).commit()

        mapFragment.getMapAsync(this)

        setRetroFit()
        Log.d("TAG", "request 시작")
        val response = service.requestRoute(coordinate = "37.422,-122.084;37.57,-126.977;").enqueue(object : Callback<Route> {
            override fun onFailure(call: Call<Route>, t: Throwable) {
                if(t is IOException){
                    Log.d("TAG", "IOException!")
                }else if(t is RuntimeException){
                    Log.d("TAG", "RuntimeException!")
                }else{
                    Log.d("TAG", "?????????")
                }

                Log.d("TAG", "실패2!")
            }
            override fun onResponse(call: Call<Route>, response: Response<Route>) {
                if(response.isSuccessful) {
                    Log.d("TAG", "시작!")
                    Log.d("getData", "getData")
                    Log.d("code", response.body()?.code)
                }else{
                    Log.d("TAG", "실패1!")
                }
            }
        })
        Log.d("TAG", "request 끝")


        // permissions
        checkPermission()
    }

    fun checkPermission() {

        //check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted so request
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super
            .onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0]
                            == PackageManager.PERMISSION_GRANTED))
                {
                    //granted get current location and show on the map



                } else {
                    //denied

                }

            }
        }
    }


    override fun onMapReady(naverMap: NaverMap) {


        // map fragment settings
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isCompassEnabled = false
        uiSettings.isIndoorLevelPickerEnabled = true
        uiSettings.isZoomControlEnabled = true


        // print 좌표 of a long clicked point, to set the place as Destination
        naverMap.setOnMapLongClickListener { _, coord ->
            Toast.makeText(
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // print location if location change happens
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(
                this, "${location.latitude}, ${location.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }


    }


}

