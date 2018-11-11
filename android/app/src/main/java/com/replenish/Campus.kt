package com.replenish

import android.location.Location
import com.google.android.gms.maps.model.LatLng

data class Campus(val fillStations: List<FillStation>) {
    val center: LatLng

    init {
        var lat = 0.0
        var lng = 0.0
        fillStations.map { it.location }.forEach {
            lat += it.latitude
            lng += it.longitude
        }
        center = LatLng(lat / fillStations.size, lng / fillStations.size)
    }
}

fun LatLng.distanceTo(other: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        latitude, longitude,
        other.latitude, other.longitude,
        results
    )
    return results[0].toDouble()
}