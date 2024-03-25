package com.a213310025melisaumami.projekpameran;

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.a213310025melisaumami.projekpameran.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in utdi and move the camera
        val utdi = LatLng(-7.793389533212323, 110.4072644274678)
        mMap.addMarker(MarkerOptions().position(utdi).title("UTDI"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(utdi))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(utdi, 12f))

        // Add a marker in muhammadiyah and move the camera
        //val muhammadiyah = LatLng(-7.79057, 110.318990)
        //mMap.addMarker(MarkerOptions().position(muhammadiyah).title("RS PKU Muhammadiyah Jogja"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(muhammadiyah))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(muhammadiyah, 12f))

        // Add a marker in Sardjito and move the camera
        //val sardjito = LatLng(-7.7688, 110.3739)
        //mMap.addMarker(MarkerOptions().position(sardjito).title("RS PKU Muhammadiyah Jogja"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sardjito))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sardjito, 12f))

        // Add a marker in Tugu Yogyakarta and move the camera
        //val tugu = LatLng(-7.782984, 110.4072644274678)
        //mMap.addMarker(MarkerOptions().position(tugu).title("Tugu Yogyakarta"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(tugu))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tugu, 12f))

        // Add a marker in Alun Alun Kidul Yogyakarta and move the camera
        //val alkid = LatLng(-7.811885, 110.363814)
        //mMap.addMarker(MarkerOptions().position(alkid).title("Alun Alun Kidul Yogyakarta"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(alkid))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alkid, 12f))

        //Add a marker in Tjitrowardojo adn move the camera
        val tjitrowardojo = LatLng(-7.7199166, 109.9953137)
        mMap.addMarker(MarkerOptions().position(tjitrowardojo).title("RSUD DR. Tjitrowardojo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(tjitrowardojo))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tjitrowardojo, 12f))

        // Add a marker in muhammadiyah and move the camera
        //val kraton = LatLng(-7.797068, 110.370529)
        //mMap.addMarker(MarkerOptions().position(kraton).title("Kraton Yogyakarta"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng( kraton))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kraton, 12f))

        // Add a marker in Taman Sari and move the camera
        //val tamansari = LatLng(-6.150760, 106.826927)
        //mMap.addMarker(MarkerOptions().position(tamansari).title("Taman Sari"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(tamansari))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tamansari, 12f))

        // Add a marker in Malioboro and move the camera
        //val malioboro = LatLng(-7.793006, 110.365981)
        //mMap.addMarker(MarkerOptions().position(malioboro).title("Malioboro"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(malioboro))
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malioboro, 12f))
    }
}