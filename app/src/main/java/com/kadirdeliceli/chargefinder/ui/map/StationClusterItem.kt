package com.kadirdeliceli.chargefinder.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation

// ChargingStation'ı clustering sistemine tanıtan sarmalayıcı.
class StationClusterItem(
    val station: ChargingStation
) : ClusterItem {
    private val position = LatLng(station.latitude, station.longitude)

    override fun getPosition(): LatLng = position
    override fun getTitle(): String = station.name
    override fun getSnippet(): String? = station.operatorName
    override fun getZIndex(): Float? = null
}