package com.naver.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream


const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

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
                            == PackageManager.PERMISSION_GRANTED)
                ) {
                    //granted get current location and show on the map


                } else {
                    //denied

                }

            }
        }
    }


    override fun onMapReady(naverMap: NaverMap) {
        //map camera bound
        naverMap.extent = LatLngBounds(LatLng(37.4460, 126.933), LatLng(37.475, 126.982))

        // map fragment settings
        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isCompassEnabled = false
        uiSettings.isIndoorLevelPickerEnabled = true
        uiSettings.isZoomControlEnabled = true


        // print 좌표 of a long clicked point, to set the place as Destination
        naverMap.setOnMapLongClickListener { point, coord ->
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


        val assetManager: AssetManager = resources.assets
        var inputStream: InputStream = assetManager.open("sample.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jArray = JSONArray(jsonString)
        for(i in 0 until jArray.length()){
            val road = jArray.getJSONObject(i)
            val seqNum = road.getString("sequence")
            val pathPoints = road.getJSONArray("pathPoints")
            var coordList = mutableListOf<LatLng>()
            for(j in 0 until pathPoints.length()){
                val pathPoint = pathPoints.getJSONArray(j)
                val longitude:Double = pathPoint.optDouble(0)
                val latitude = pathPoint.optDouble(1)
                coordList.add(LatLng(latitude, longitude))

            }
            PathOverlay().apply {
                coords = coordList
                map = naverMap
                color = Color.GREEN
            }
        }

    }


}

