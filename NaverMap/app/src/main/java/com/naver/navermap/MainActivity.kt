package com.naver.navermap

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import android.content.res.AssetManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PathOverlay
import org.json.JSONArray
import java.io.InputStream
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.navermap.data.RetroResult
import com.naver.maps.map.util.FusedLocationSource
import android.R.layout
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationSource: FusedLocationSource
    val LOCATION_PERMISSION_REQUEST_CODE = 1000
    var mLocationPermissionGranted = false


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

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            mLocationPermissionGranted = true
            Toast.makeText(this, "Permission Granted!", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onMapReady(naverMap: NaverMap) {
        var currLocation: LatLng
        //map camera bound
        naverMap.extent = LatLngBounds(LatLng(37.4460, 126.933), LatLng(37.475, 126.982))

        naverMap.locationSource = locationSource
        // map fragment settings
        val uiSettings = naverMap.uiSettings.apply {
            isLocationButtonEnabled = true
            isCompassEnabled = false
            isIndoorLevelPickerEnabled = true
            isZoomControlEnabled = true
        }

        val retro = RetroFitAPI.getInstance(applicationContext)
        //callback 함수 설정
        retro.apply {
            setListener {
                when (val res = it[0].result) {
                    is RetroResult.NoInternetError -> {
                        Toast.makeText(
                            this@MainActivity, "No Internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is RetroResult.NoResponseError -> {
                        Toast.makeText(
                            this@MainActivity, "No Response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is RetroResult.Success -> {
                        val path = PathOverlay().apply {
                            coords = it.map {
                                LatLng(it.latLng.latitude, it.latLng.longitude)
                            }
                            map = naverMap
                            color = Color.RED
                        }
                    }
                }
            }
        }
        //dummy coordination
        retro.getRetroFitClient(37.5586, 126.9781, 37.5701525, 126.98304)

        // print 좌표 of a long clicked point, to set the place as Destination
        naverMap.setOnMapLongClickListener { _, coord ->
            Toast.makeText(
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }


        naverMap.locationTrackingMode = LocationTrackingMode.Face


        //
        val assetManager: AssetManager = resources.assets
        var inputStream: InputStream = assetManager.open("sample.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jArray = JSONArray(jsonString)
        for (i in 0 until jArray.length()) {
            val road = jArray.getJSONObject(i)
            val seqNum = road.getString("sequence")
            val pathPoints = road.getJSONArray("pathPoints")
            var coordList = mutableListOf<LatLng>()
            for (j in 0 until pathPoints.length()) {
                val pathPoint = pathPoints.getJSONArray(j)
                val longitude: Double = pathPoint.optDouble(0)
                val latitude = pathPoint.optDouble(1)
                coordList.add(LatLng(latitude, longitude))

            }
            PathOverlay().apply {
                coords = coordList
                map = naverMap
                color = Color.GREEN
            }
        }

        //TODO: algorithm > compare current position to road data
        // set current location using map matching algorithm
        val v = Viterbi("sample.jason")

        val mainHandler = Handler(Looper.getMainLooper())
        val delay: Long = 1000
        var inputLocation: Location

        mainHandler.post(object : Runnable {
            override fun run() {
                //get location
                inputLocation = locationSource.lastLocation!!

                //show raw location input
                val marker = Marker()
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.RED
                marker.width = 20
                marker.height = 35
                marker.position = LatLng(inputLocation.latitude, inputLocation.longitude)
                marker.map = naverMap

                //get location mapped to road
                currLocation = v.run {
                    getMapMatchingLocation(inputLocation)
                }
                //show on map
                naverMap.locationOverlay.apply {
                    position = LatLng(currLocation.latitude, currLocation.longitude)

                }

                mainHandler.postDelayed(this, delay)
            }
        })

    }

    /*//TODO: set as destination
    // print 좌표 of a long clicked point, to set the place as Destination
    naverMap.setOnMapLongClickListener { _, coord ->
        Toast.makeText(
            this, "${coord.latitude}, ${coord.longitude}",
            Toast.LENGTH_SHORT
        ).show()
    }*/

    // print location if location change happens
    /* naverMap.addOnLocationChangeListener { location ->
        locationOverlay.apply {
            position = LatLng(location.latitude, location.longitude)

        }
    }*/


}


//to use source location

/*private fun checkPermission() {

     //check if permission is granted
     if (ContextCompat.checkSelfPermission(
             this,
             Manifest.permission.ACCESS_FINE_LOCATION
         )
         != PackageManager.PERMISSION_GRANTED
         && ContextCompat.checkSelfPermission(
             this,
             Manifest.permission.ACCESS_COARSE_LOCATION
         )
         != PackageManager.PERMISSION_GRANTED
     ) {
         // Permission is not granted, so request
         ActivityCompat.requestPermissions(
             this,
             PERMISSIONS,
             REQUEST
         )

     }
 }*/

