package com.a213310025melisaumami.projekpameran

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mqttClient: MqttClient
    private lateinit var mulai: Button
    private lateinit var searchBar: android.widget.SearchView
    private var isSendingData = false

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var locationNames: ArrayList<String>
    private lateinit var locations: ArrayList<LocationData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)

        locations = arrayListOf(
            LocationData("UTDI", -7.793389533212323, 110.4072644274678),
            LocationData("RS PKU Muhammadiyah Jogja", -7.79057, 110.318990),
            LocationData("Rumah Sakit Umum Pusat (RSUP) Dr. Sardjito ", -7.7688, 110.3739),
            LocationData("Tugu Yogyakarta", -7.782984, 110.4072644274678),
            LocationData("Alun Alun Kidul Yogyakarta", -7.811885, 110.363814),
            LocationData("Kraton Yogyakarta", -7.797068, 110.370529),
            LocationData("Taman Sari", -6.150760, 106.826927),
            LocationData("Malioboro", -7.793006, 110.365981),
            LocationData("RSUD DR. Tjitrowardojo", -7.7199166, 109.9953137)
        )

        locationNames = ArrayList(locations.map { it.name })

        // Inisialisasi adapter ListView dengan layout item yang telah dibuat
        adapter = ArrayAdapter(this, R.layout.activity_list, R.id.itemTextView, locationNames)

        // Set adapter pada ListView
        listView.adapter = adapter

        val btnNext: Button = findViewById(R.id.btnNext)
        btnNext.setOnClickListener {}

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()

        val brokerUrl = "tcp://broker.mqtt-dashboard.com:1883"
        val clientId = MqttClient.generateClientId()
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

        val options = MqttConnectOptions()
        options.isCleanSession = true

        mqttClient.connect(options)

        mulai = findViewById(R.id.btnNext)
        searchBar = findViewById(R.id.searchBar)

        mulai.setOnClickListener {
            if (isSendingData) {
                stopSendingData()
            } else {
                startSendingData()
            }

            // Panggil fungsi kedua di sini
            performAction2()
        }

        // Menambahkan pendengar teks pada EditText
        searchBar.setOnQueryTextListener(object : OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val location = searchLocation(it)
                    if (location != null) {
                        navigateToMaps(location)
                        searchBar.clearFocus()
                        return true
                    }
                }
                Toast.makeText(this@MainActivity, "Location not found", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            if (selectedItem != null) {
                intent.putExtra("locationName", selectedItem)
            }
            val selectedLocation = locations[position]
            intent.putExtra("latitude", selectedLocation.latitude)
            intent.putExtra("longitude", selectedLocation.longitude)
            startActivity(intent)
        }

        startLocationUpdates()
    }

    private fun searchLocation(query: String): LocationData? {
        for (location in locations) {
            if (location.name.equals(query, ignoreCase = true)) {
                return location
            }
        }
        return null
    }

    private fun navigateToMaps(location: LocationData) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("locationName", location.name)
        startActivity(intent)
    }

    private fun startSendingData() {
        isSendingData = true
        getLastLocation()
    }

    private fun stopSendingData() {
        isSendingData = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        //showToast("Sending data stopped")
    }

    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        sendLocationToServer(location)
                        if (isSendingData) {
                            getLastLocation()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun startLocationUpdates() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    sendLocationToServer(location)

                    if (isSendingData) {
                        getLastLocation()
                    }
                }
            }
        }
    }

    private fun requestNewLocationData() {
        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // tohandle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.disconnect()
    }

    private fun sendLocationToServer(location: Location) {
        val topic1 = "latitude"
        val topic2 = "longitude"
        val latitude = "${location.latitude}"
        val longitude = "${location.longitude}"
        val mqttMessage1 = MqttMessage(latitude.toByteArray())
        val mqttMessage2 = MqttMessage(longitude.toByteArray())
        mqttClient.publish(topic1, mqttMessage1)
        mqttClient.publish(topic2, mqttMessage2)
        //showToast("terkirim")
    }

    private fun performAction2() {
        val btnNext: Button = findViewById(R.id.btnNext)
        btnNext.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private data class LocationData(val name: String, val latitude: Double, val longitude: Double)
}
