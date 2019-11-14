package com.naver.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource


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

        naverMap.locationSource = locationSource

        // map fragment settings
        naverMap.uiSettings.apply {

            isCompassEnabled = false
            isIndoorLevelPickerEnabled = true
            isZoomControlEnabled = true
        }

        val locationOverlay = naverMap.locationOverlay

        if (mLocationPermissionGranted) {
            naverMap.uiSettings.isLocationButtonEnabled = true
            //locationOverlay.isVisible = true
        }


        naverMap.locationTrackingMode = LocationTrackingMode.Face

        // print location if location change happens
        //TODO: algorithm > compare current position to road data
        // TODO: Show new position
        naverMap.addOnLocationChangeListener { location ->
            locationOverlay.apply {
                position = LatLng(location.latitude, location.longitude)

            }
        }

        // print 좌표 of a long clicked point, to set the place as Destination
        //TODO: set as destination
        naverMap.setOnMapLongClickListener { _, coord ->
            Toast.makeText(
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



}

//to use source location instead of fusedlocation

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

