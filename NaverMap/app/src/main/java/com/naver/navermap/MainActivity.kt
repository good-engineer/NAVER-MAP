package com.naver.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.map.LocationTrackingMode


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var naverMap: NaverMap


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
        if ((grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED)
        ) { //granted
            // show current location

            Toast.makeText(this,"Permission Granted!",Toast.LENGTH_LONG)


        } else {
            //denied

            Toast.makeText(this,"Permission Denied!",Toast.LENGTH_LONG)

        }
    }


    override fun onMapReady(naverMap: NaverMap) {

        checkPermission()


        // map fragment settings
         naverMap.uiSettings.apply {

            isCompassEnabled = false
            isIndoorLevelPickerEnabled = true
            isZoomControlEnabled = true
            isLocationButtonEnabled= true
        }
        // print 좌표 of a long clicked point, to set the place as Destination
        naverMap.setOnMapLongClickListener { _, coord ->
            Toast.makeText(
                    this, "${coord.latitude}, ${coord.longitude}",
                    Toast.LENGTH_SHORT
            ).show()
        }

        // print location if location change happens
         naverMap.locationTrackingMode = LocationTrackingMode.Face

        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(
                    this, "${location.latitude}, ${location.longitude}",
                    Toast.LENGTH_SHORT
            ).show()
        }



        /*  //not working > getting current location and print

          val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
          val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

          val marker = Marker()
          marker.position = LatLng(location.latitude, location.latitude)
          marker.map = naverMap
          lm?.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,gpsLocation)

      private val gpsLocation :LocationListener = object : LocationListener{
          override fun onLocationChanged(location:Location?)  {

              val marker = Marker()
              marker.position = LatLng(location.latitude, location.latitude)
              marker.map = naverMap
          }
      }*/

    }


}

