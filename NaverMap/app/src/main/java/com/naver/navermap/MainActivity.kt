package com.naver.navermap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.navermap.data.Direction
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

        // print 좌표 of a long clicked point, to set the place as Destination
        naverMap.setOnMapLongClickListener { _, coord ->
            Toast.makeText(
                this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT
            ).show()
            naverMap.locationTrackingMode = LocationTrackingMode.Face


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

