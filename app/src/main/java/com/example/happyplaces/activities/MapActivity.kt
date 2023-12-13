package com.example.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.models.happyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_happy_place_detail.*
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mHappyPlaceDetails: happyPlaceModel?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
        {
            mHappyPlaceDetails= intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as? happyPlaceModel
        }

        if(mHappyPlaceDetails!= null)
        {
            setSupportActionBar(toolbar_happy_place_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetails!!.title
            toolbar_happy_place_map.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment= supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googlemap: GoogleMap) {
        val position= LatLng(mHappyPlaceDetails!!.latitude, mHappyPlaceDetails!!.longitude)
        googlemap.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetails!!.location))
        val newLangZoom= CameraUpdateFactory.newLatLngZoom(position, 10f)
        googlemap.animateCamera(newLangZoom)
    }
}