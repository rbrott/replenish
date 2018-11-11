package com.replenish

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.fitbit.authentication.AuthenticationManager
import com.fitbit.authentication.LogoutTaskCompletionHandler
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
    private lateinit var waterMonitor: WaterMonitor

    private var campus: Campus? = null
    private var currentLocation: LatLng? = null
    private var map: GoogleMap? = null

    companion object {
        fun getDirectionsIntent(destination: LatLng): Intent {
            val gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude + "&mode=w")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            return mapIntent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        campusManager = CampusManager(this)

        directionsButton.setOnClickListener {
            val currentLocation = currentLocation ?: LatLng(0.0, 0.0)
            val fillLocation = campus?.getClosestFillStation(currentLocation)?.location ?: LatLng(0.0, 0.0)
            startDirections(fillLocation)
        }

        NotificationService.setAlarm(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else {
            findCurrentLocation()
        }

        waterMonitor = WaterMonitor(this)
    }

    override fun onStart() {
        super.onStart()

        sync()
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
        when (item.itemId) {
            R.id.action_reset -> {
                waterMonitor.clear()
                val alarmManager = getSystemService(AlarmManager::class.java)
                val intent = Intent(this, LoginActivity::class.java).let {
                    PendingIntent.getActivity(this, 0, it, 0)
                }
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2000, intent)
                AuthenticationManager.logout(this, object : LogoutTaskCompletionHandler {
                    override fun logoutSuccess() {
                        finish()
                    }

                    override fun logoutError(message: String?) {
                        finish()
                    }
                })
            }
            R.id.action_refresh -> {
                sync()
            }
            R.id.action_add_water -> {
                openWaterDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // we have our own direction functionality
        map!!.setOnMarkerClickListener { true }

        enableMyLocation()

        addMarkers()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // Access to the location has been granted to the app.
        map?.isMyLocationEnabled = true
    }

    private fun findCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        campus = campusManager.getClosestCampus(currentLocation!!)

                        addMarkers()
                    }
                }
        }
    }

    private fun addMarkers() {
        val publicIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        val privateIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)

        val fillStations = campus?.fillStations ?: emptyList()
        for (fillStation in fillStations) {
            map?.addMarker(
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

        val campusCenter = campus?.center ?: LatLng(0.0, 0.0)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(campusCenter, 16.0f))
    }

    private fun startDirections(destination: LatLng) {
        startActivity(getDirectionsIntent(destination))
    }

    private fun sync() {
        waterMonitor.update {
            val cupsConsumed = waterMonitor.getWaterConsumed().toInt() / 8
            val cupsToDrink = waterMonitor.getWaterNeeded().toInt() / 8

            cupsConsumedTextView.text = cupsConsumed.toString()
            cupsToDrinkTextView.text = cupsToDrink.toString()

            if (cupsConsumed == 1) {
                cupsConsumedLabel.text = "Cup Consumed"
            } else {
                cupsConsumedLabel.text = "Cups Consumed"
            }

            if (cupsToDrink == 1) {
                cupsToDrinkLabel.text = "Cup To Drink"
            } else {
                cupsToDrinkLabel.text = "Cups To Drink"
            }
        }
    }

    private fun openWaterDialog() {
        val dialog = Dialog(this)
        val inflater = getSystemService(LayoutInflater::class.java)
        val layout = inflater.inflate(R.layout.water_dialog, water_dialog)!!

        val textView = layout.findViewById<TextView>(R.id.waterTextView)

        val seekbar = layout.findViewById<SeekBar>(R.id.waterSeekBar)
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

        layout.findViewById<Button>(R.id.waterDialogConfirm).setOnClickListener {
            waterMonitor.recordWaterConsumption(seekbar.progress.toFloat())
            sync()
            dialog.hide()
        }

        layout.findViewById<Button>(R.id.waterDialogCancel).setOnClickListener {
            dialog.hide()
        }

        dialog.setContentView(layout)
        dialog.show()
    }
}
