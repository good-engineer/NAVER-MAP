package com.naver.navermap

import android.app.Service.START_STICKY
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import android.R.drawable
import android.app.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Binder


class MyLocationService : Service() {
    private val binder=LocationServiceBinder()
    private val TAG = "MyLocationService"
    private lateinit var mLocationManager: LocationManager

    companion object {
        private const val UPDATE_INTERVAL: Long = 2000  // 2 sec
        private const val LOCATION_DISTANCE = 10f
    }

    inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    var mLocationListeners = arrayOf(LocationListener(LocationManager.PASSIVE_PROVIDER))

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        createNotificationChannel()

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex)
                }
            }
        }
    }

    fun StartLocationUpdate():Location{

        initializeLocationManager()
        try {
            mLocationManager?.requestLocationUpdates(
                LocationManager.PASSIVE_PROVIDER,
                UPDATE_INTERVAL,
                LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist" + ex.message)
        }
        return mLocationListeners[0].mLastLocation
    }

    private fun initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    //private fun sendNotification(direction: Direction)
    private fun sendNotification(textContent: String) {
        var icon = R.drawable.test
        /*var icon = when (direction) {
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
        }*/

        // Create an explicit intent
        //val intent = Intent(this, RouteFragment::class.java).apply {
        val intent = Intent(this, MainActivity::class.java).apply {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                "1",
                "channel1",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "this is channel 1"
            }
            channel1.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel1)
        }
    }
    inner class LocationServiceBinder : Binder(){
        fun  getService () : MyLocationService = this@MyLocationService
    }
}
