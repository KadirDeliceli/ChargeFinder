package com.kadirdeliceli.chargefinder.data.repository

import com.kadirdeliceli.chargefinder.BuildConfig
import com.kadirdeliceli.chargefinder.data.remote.ConnectionDto
import com.kadirdeliceli.chargefinder.data.remote.PoiDto
import com.kadirdeliceli.chargefinder.data.remote.RetrofitInstance
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation
import com.kadirdeliceli.chargefinder.domain.model.Connector

class StationRepository {

    // GERÇEK OCM API çağrısı
    suspend fun getNearbyStations(latitude: Double, longitude: Double): List<ChargingStation> {
        val pois = RetrofitInstance.api.getNearbyStations(
            apiKey = BuildConfig.OCM_API_KEY,
            latitude = latitude,
            longitude = longitude,
            distance = 50,
            distanceUnit = "km",
            maxResults = 100,
            compact = false,   // nested objeler (OperatorInfo, StatusType) gelsin
            verbose = false    // ama aşırı detay olmasın
        )
        return pois.map { it.toDomainModel() }
    }

    private fun PoiDto.toDomainModel(): ChargingStation {
        return ChargingStation(
            id = this.id,
            name = this.addressInfo?.title ?: "Bilinmeyen İstasyon",
            latitude = this.addressInfo?.latitude ?: 0.0,
            longitude = this.addressInfo?.longitude ?: 0.0,
            address = this.addressInfo?.addressLine1,
            operatorName = this.operatorInfo?.title,
            isOperational = this.statusType?.isOperational ?: true,
            totalPoints = this.numberOfPoints ?: 0,
            connectors = this.connections?.map { it.toConnector() } ?: emptyList(),
            isChargingSupported = false
        )
    }

    private fun ConnectionDto.toConnector(): Connector {
        return Connector(
            type = this.connectionType?.title ?: "Bilinmiyor",
            powerKw = this.powerKw,
            quantity = this.quantity ?: 1,
            isFastCharge = this.level?.isFastChargeCapable ?: false
        )
    }
}