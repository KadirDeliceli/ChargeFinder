package com.kadirdeliceli.chargefinder.ui.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class StationClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StationClusterItem>
) : DefaultClusterRenderer<StationClusterItem>(context, map, clusterManager) {

    private val iconGenerator = IconGenerator(context)

    // Tek bir istasyon (küme değil) çizilmeden önce: bizim özel pin'imizi ata
    override fun onBeforeClusterItemRendered(
        item: StationClusterItem,
        markerOptions: com.google.android.gms.maps.model.MarkerOptions
    ) {
        val icon = createStationMarker(
            color = operatorColor(item.station.operatorName),
            isOperational = item.station.isOperational
        )
        markerOptions.icon(icon).title(item.station.name)
    }

    // Kaç istasyondan itibaren kümelensin? (2 ve üzeri yakınsa kümele)
    override fun shouldRenderAsCluster(cluster: Cluster<StationClusterItem>): Boolean {
        return cluster.size >= 2
    }
}