package com.naver.navermap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.navermap.data.RetroResult

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

        // map fragment settings
        val uiSettings = naverMap.uiSettings.apply {
            isLocationButtonEnabled = true
            isCompassEnabled = false
            isIndoorLevelPickerEnabled = true
            isZoomControlEnabled = true
        }

        val retro = RetroFitAPI.getInstance()
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
            setContext(applicationContext)
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

        // print location if location change happens
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(
                this, "${location.latitude}, ${location.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}

