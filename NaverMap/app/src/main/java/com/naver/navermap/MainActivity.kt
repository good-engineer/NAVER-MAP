package com.naver.navermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class MainActivity : AppCompatActivity(),OnMapReadyCallback{
    private lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)

    }



    override fun onMapReady(naverMap: NaverMap) {

        val uiSettings = naverMap.uiSettings
        uiSettings.isCompassEnabled = false
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isIndoorLevelPickerEnabled = true
        uiSettings.isZoomControlEnabled = true

        // print 좌표 of a long clicked point, to use for setting the place as Destination
        naverMap.setOnMapLongClickListener { point, coord ->
            Toast.makeText(this, "${coord.latitude}, ${coord.longitude}",
                Toast.LENGTH_SHORT).show()
        }
        // print location if location change happens
        naverMap.addOnLocationChangeListener { location ->
            Toast.makeText(this, "${location.latitude}, ${location.longitude}",
                Toast.LENGTH_SHORT).show()
        }



        // sample for printing 좌표
        //val coord = LatLng(37.5670135, 126.9783740)
        //Toast.makeText(applicationContext,"위도: ${coord.latitude}, 경도: ${coord.longitude}", Toast.LENGTH_SHORT).show()
    }


}

