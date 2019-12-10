package com.naver.navermap

import android.content.IntentSender
import android.content.pm.PackageManager
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
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var naverMap: NaverMap? =null
    private  lateinit var v: Viterbi
    //private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    //private lateinit var mcurrLocation: Location
    private lateinit var locationCallback: LocationCallback

   // val LOCATION_PERMISSION_REQUEST_CODE = 1000
    //var mLocationPermissionGranted = false
    val REQUEST_CHECK_SETTINGS = 0x1
    var UPDATE_INTERVAL: Long = 10000  // 10 sec
    var FASTEST_INTERVAL: Long = 1000 // 1 sec


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


        // change?
        //locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        val assetManager: AssetManager = resources.assets
        var inputStream: InputStream = assetManager.open("sample.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
         v = Viterbi(jsonString)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        createLocationCallBack()

    }


    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() {

        // location request
        locationRequest = LocationRequest.create()
        locationRequest.run {
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            setInterval(UPDATE_INTERVAL)
            setFastestInterval(FASTEST_INTERVAL)
        }

        // location setting request builder
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        //Toast.makeText(this, "location setting request builder", Toast.LENGTH_LONG).show()

        // initialize location service object
        val settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener {

            startLocationUpdates()

        }.addOnFailureListener {
            if (it is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    it.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun createLocationCallBack() {
       locationCallback = object : LocationCallback() {
           override fun onLocationResult(Result: LocationResult?) {
               Result ?: return

               displayLocation(Result.lastLocation)

               for (location in Result.locations) {
                   // Update UI with location data
                   // ...
                   displayLocation(location)
               }
           }
        }

    }


    protected fun startLocationUpdates() {
        Toast.makeText(this, "startupdatelocation", Toast.LENGTH_LONG).show()

        if (checkPermission()) {
            fusedLocationClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }


    private fun displayLocation(location: Location) {
        //Toast.makeText(this, "display location ", Toast.LENGTH_LONG).show()
        var currLocation: LatLng


        if (location != null) {

            naverMap?.let {
                //show raw location input
                //show on map

                val marker = Marker()
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.RED
                marker.width = 40
                marker.height = 50
                marker.position = LatLng(location)
                marker.map=it
               // Toast.makeText(this, "display location marker", Toast.LENGTH_LONG).show()

                //get location mapped to road
                currLocation = v!!.run {
                    getMapMatchingLocation(location)
                }
                val coord = currLocation
                val locationOverlay = it.locationOverlay
                locationOverlay.isVisible = true
                locationOverlay.position = coord
                locationOverlay.bearing = location.bearing
                it.moveCamera(CameraUpdate.scrollTo(coord))
                //Toast.makeText(this, "display mapmatching result", Toast.LENGTH_LONG).show()
            }

            //if location < 5 m to end of path road length
            //projection
        }

    }

    private fun checkPermission(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    1
                )

            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                startLocationUpdates()
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

     override fun onMapReady(naverMap: NaverMap) {
        //Toast.makeText(this, "on map ready!", Toast.LENGTH_LONG).show()

        //map camera bound
         this.naverMap=naverMap

        naverMap.extent = LatLngBounds(LatLng(37.4460, 126.933), LatLng(37.475, 126.982))

        //naverMap.locationSource = locationSource

        // map fragment settings
        naverMap?.let {
            val uiSettings = it.uiSettings.apply {
                isLocationButtonEnabled = false
                isCompassEnabled = false
                isIndoorLevelPickerEnabled = true
                isZoomControlEnabled = true
            }
            it.locationTrackingMode = LocationTrackingMode.Face
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
                                // prev road - curr road
                                // on change direction
                                // path list L, D
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


    }


/*

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

    }*/
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


