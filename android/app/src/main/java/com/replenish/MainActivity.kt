package com.replenish

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.water_dialog.*


const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var campusManager: CampusManager
    private lateinit var campus: Campus
    private lateinit var currentLocation: LatLng
    private lateinit var map: GoogleMap
    private var mapReady = false
    private var locationReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        campusManager = CampusManager(this)

        directionsButton.setOnClickListener {
            if (campus != null) {
                startDirections(campus.fillStations.minBy { currentLocation.distanceTo(it.location) }!!.location)
            }
        }

        NotificationService.setAlarm(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            findCurrentLocation()
        }

        water_input_button.setOnClickListener {
            openWaterDialog()
        }

        val accessToken = intent.getStringExtra("accessToken")
        Log.i("Replenish", "Access token: $accessToken")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            findCurrentLocation()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true

        map.setOnMarkerClickListener { true }

        enableMyLocation()

        initMap()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        } else if (mapReady) {
            // Access to the location has been granted to the app.
            map.isMyLocationEnabled = true
        }
    }

    private fun findCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        campus = campusManager.getClosestCampus(currentLocation)
                        locationReady = true

                        initMap()
                    }
                }
        }
    }

    private fun initMap() {
        if (mapReady && locationReady) {
            val publicIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            val privateIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)

            for (fillStation in campus.fillStations) {
                map.addMarker(
                    MarkerOptions()
                        .position(fillStation.location)
                        .icon(
                            when (fillStation.type) {
                                FillStationType.PUBLIC -> publicIcon
                                FillStationType.PRIVATE -> privateIcon
                            }
                        )
                )
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(campus.center, 16.0f))
        }
    }

    private fun startDirections(destination: LatLng) {
        val gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude + "&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun openWaterDialog() {
        val dialog = Dialog(this)
        val inflater = getSystemService(LayoutInflater::class.java)
        val layout = inflater.inflate(R.layout.water_dialog, water_dialog)!!

        val textView = layout.findViewById<TextView>(R.id.waterTextView)

        val seekbar = layout.findViewById<SeekBar>(R.id.water_seekbar)
        seekbar.progress = 0
        seekbar.keyProgressIncrement = 2
        seekbar.max = 24
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = "${progress}oz"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        layout.findViewById<Button>(R.id.dialog_ok).setOnClickListener {
            // TODO
            dialog.hide()
        }

        layout.findViewById<Button>(R.id.dialog_cancel).setOnClickListener {
            dialog.hide()
        }

        dialog.setContentView(layout)
        dialog.show()
    }
}
