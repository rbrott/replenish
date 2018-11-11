package com.replenish

import com.google.android.gms.maps.model.LatLng

data class FillStation(val name: String, val location: LatLng, val type: FillStationType)