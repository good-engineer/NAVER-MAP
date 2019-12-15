package com.naver.navermap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.navermap.data.Direction
import com.naver.navermap.data.RetroResult
import org.json.JSONArray
import java.io.InputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationSource: FusedLocationSource
    var mLocationPermissionGranted = false
    private var currLocation: Location? = null
    private var path: PathOverlay? = null
    val fm = supportFragmentManager
    private var naverMap: NaverMap?=null
    private lateinit var v: Viterbi
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var lastKnownLocation: LatLng
    private lateinit var locationCallback: LocationCallback
    private lateinit var jsonString: String
    //todo:location request button
    // private var requestingLocationUpdates:Boolean =false

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL: Long = 10000  // 10 sec
        private const val FASTEST_INTERVAL: Long = 1000 // 1 sec
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        //map fragment manager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        //search fragment
        fm.beginTransaction().add(R.id.container, SearchFragment()).commit()
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        //set the viterbi input road data
        val assetManager: AssetManager = resources.assets
        var inputStream: InputStream = assetManager.open("sample.json")
        jsonString = inputStream.bufferedReader().use { it.readText() }
        v = Viterbi(jsonString)

        //location periodic update
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallBack()
    }

    override fun onResume() {
        super.onResume()
        //if(requestingLocationUpdates)
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
                for (location in Result.locations) {
                    // Update UI with location data
                    displayLocation(location)
                }
            }
        }

    }

    protected fun startLocationUpdates() {
        if (checkPermission()) {
            fusedLocationClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun displayLocation(location: Location) {
        currLocation = location

        if (location != null) {

            naverMap?.let {
                //show raw location input
                val marker = Marker()
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.RED
                marker.width = 40
                marker.height = 50
                marker.position = LatLng(location)
                marker.map = it
                //get location mapped to road
                lastKnownLocation = v!!.run {
                    getMapMatchingLocation(location)
                }
                //show on map
                val coord = lastKnownLocation
                val locationOverlay = it.locationOverlay
                locationOverlay.isVisible = true
                locationOverlay.position = coord
                locationOverlay.bearing = location.bearing
                it.moveCamera(CameraUpdate.scrollTo(coord))

                //TODO: if lastKnownLocation and currRoad
                // distance is < 10 m send change direction Alarm

            }

        }

    }


    private fun checkPermission(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        //check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                permissions[0]
            )
            != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                permissions[1]
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, so request
            ActivityCompat.requestPermissions(
                this,
                permissions,
                1
            )
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

    override fun onBackPressed() {
        fm.findFragmentById(R.id.route_fragment)?.let {
            fm.beginTransaction().remove(it).commit()
        } ?: super.onBackPressed()
    }

    override fun onMapReady(naverMap: NaverMap) {
        //map camera bound
        this.naverMap = naverMap
        naverMap.extent = LatLngBounds(LatLng(37.4460, 126.933), LatLng(37.475, 126.982))

        naverMap.locationSource = locationSource

        // map fragment settings
        naverMap?.let {
            val uiSettings = it.uiSettings.apply {
                isLocationButtonEnabled = false
                isCompassEnabled = false
                isIndoorLevelPickerEnabled = true
                isZoomControlEnabled = true
            }
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
                                // prev road - curr road
                                // on change direction
                                // path list L, D
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
        val locationOverlay = naverMap.locationOverlay

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

    private fun sendNotification(direction: Direction) {

        var icon = when (direction) {
            Direction.FRONT -> R.drawable.direction_front
            Direction.LEFT -> R.drawable.direction_left
            Direction.NIL -> R.drawable.direction_nil
            Direction.RIGHT -> R.drawable.direction_right
            Direction.BACK -> R.drawable.direction_back
        }
        var textContent = when (direction) {
            Direction.FRONT -> "Keep going straight!"
            Direction.LEFT -> "Turn left in 10 meters!"
            Direction.NIL -> "No direction is available!"
            Direction.RIGHT -> "Turn right in 10 meters!"
            Direction.BACK -> "Turn back!"
        }

        // Create an explicit intent
        val intent = Intent(this, RouteFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        //create notification
        var builder = NotificationCompat.Builder(this, "1")
            .setSmallIcon(icon)
            .setContentTitle("Direction Change!")
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //send notification
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel1"
            val descriptionText = "this is channel 1"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel1 = NotificationChannel("1", name, importance).apply {
                description = descriptionText
            }
            channel1.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel1)
        }
    }
}

