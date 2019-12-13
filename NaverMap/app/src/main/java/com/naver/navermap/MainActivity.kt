package com.naver.navermap

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.navermap.data.RetroResult

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    var mLocationPermissionGranted = false
    private var currLocation: Location? = null
    private var path: PathOverlay? = null
    val fm = supportFragmentManager

    companion object {
        val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //map fragment manager
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

    override fun onBackPressed() {
        fm.findFragmentById(R.id.route_fragment)?.let {
            fm.beginTransaction().remove(it).commit()
        } ?: super.onBackPressed()
    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.locationSource = locationSource

        //map camera bound
        naverMap.extent = LatLngBounds(LatLng(37.4460, 126.933), LatLng(37.475, 126.982))

        // map fragment settings
        val uiSettings = naverMap.uiSettings.apply {
            isLocationButtonEnabled = true
            isCompassEnabled = false
            isIndoorLevelPickerEnabled = true
            isZoomControlEnabled = true
        }

        val retro = RetroFitAPI.getInstance(application)
        //callback 함수 설정
        retro.apply {
            setListener {
                when (it[0].result) {
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
                        path?.map = null
                        path = PathOverlay().apply {
                            coords = it.map {
                                LatLng(it.latLng.latitude, it.latLng.longitude)
                            }
                            map = naverMap
                            color = Color.RED
                        }

                        fm.beginTransaction()
                            .add(R.id.route_fragment, RouteFragment.newInstance(it.map {
                                LatLng(it.latLng.latitude, it.latLng.longitude)
                            }))
                            .commit()
                    }
                }
            }
        }

        val locationOverlay = naverMap.locationOverlay

        naverMap.locationTrackingMode = LocationTrackingMode.Face

        // print location if location change happens
        //TODO: algorithm > compare current position to road data
        // TODO: Show new position
        naverMap.addOnLocationChangeListener { location ->
            locationOverlay.apply {
                position = LatLng(location.latitude, location.longitude)
            }
            currLocation = location
        }

        naverMap.setOnMapLongClickListener { _, coord ->
            currLocation?.let {
                retro.getRetroFitClient(
                    it.latitude,
                    it.longitude,
                    coord.latitude,
                    coord.longitude
                )
            }

            val searchFragment = fm.findFragmentById(R.id.container) as SearchFragment?
            searchFragment?.let {
                it.setText("%.6f, ".format(coord.latitude) + "%.6f".format(coord.longitude))
            }

            Toast.makeText(
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
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

