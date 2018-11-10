package com.hydration

import android.content.Context
import com.google.android.gms.maps.model.LatLng

const val FILL_STATIONS_DIRNAME = "stations"

class FillStationManager(context: Context) {
    val fillStations: List<FillStation>

    init {
        val assetsManager = context.assets!!
        fillStations = assetsManager.list(FILL_STATIONS_DIRNAME)!!
            .map { filename ->
                val reader = CSVReader(assetsManager.open("$FILL_STATIONS_DIRNAME/$filename"))
                reader.rows.mapIndexed { i, row ->
                    val name = "${filename.split(".")[0]}-$i"
                    val lat = row[0].toDouble()
                    val lng = row[1].toDouble()
                    FillStation(name, LatLng(lat, lng))
                }
            }.flatten()
    }
}