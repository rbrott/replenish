package com.replenish

import android.content.Context
import com.google.android.gms.maps.model.LatLng

const val CAMPUS_DIRNAME = "campus"

class CampusManager(context: Context) {
    val campuses: List<Campus>

    init {
        val assetsManager = context.assets!!
        campuses = assetsManager.list(CAMPUS_DIRNAME)!!
            .map { filename ->
                val reader = CSVReader(assetsManager.open("$CAMPUS_DIRNAME/$filename"))
                val fillStations = reader.rows.mapIndexed { i, row ->
                    val name = "${filename.split(".")[0]}-$i"
                    val lat = row[0].toDouble()
                    val lng = row[1].toDouble()
                    val type = when (row[2]) {
                        "public" -> FillStationType.PUBLIC
                        "private" -> FillStationType.PRIVATE
                        else -> FillStationType.PRIVATE
                    }
                    FillStation(name, LatLng(lat, lng), type)
                }
                Campus(fillStations)
            }
    }

    fun getClosestCampus(location: LatLng) = campuses.minBy { it.center.distanceTo(location) }!!
}