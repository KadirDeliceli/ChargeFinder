package com.kadirdeliceli.chargefinder.data.repository

import com.kadirdeliceli.chargefinder.data.remote.AddressInfoDto
import com.kadirdeliceli.chargefinder.data.remote.ConnectionDto
import com.kadirdeliceli.chargefinder.data.remote.ConnectionTypeDto
import com.kadirdeliceli.chargefinder.data.remote.LevelDto
import com.kadirdeliceli.chargefinder.data.remote.OperatorInfoDto
import com.kadirdeliceli.chargefinder.data.remote.PoiDto
import com.kadirdeliceli.chargefinder.data.remote.StatusTypeDto
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation
import com.kadirdeliceli.chargefinder.domain.model.Connector
import kotlinx.coroutines.delay

class StationRepository {

    // şuanda yalancı veri kullnacaz
    // OCM key'i gelince bu fonksiyonun içeriğini gerçek API çağrısıyla değiştireceğiz,
    suspend fun getNearbyStations(latitude: Double, longitude: Double): List<ChargingStation> {
        delay(800) // gerçek network gecikmesini simüle ediyoruz, loading state'i test edebilmek için
        return getMockPois(latitude, longitude).map { it.toDomainModel() }
    }

    private fun getMockPois(lat: Double, lng: Double): List<PoiDto> {
        return listOf(
            PoiDto(
                id = 1,
                addressInfo = AddressInfoDto(
                    title = "Migros AVM Şarj İstasyonu",
                    latitude = lat + 0.01,
                    longitude = lng + 0.01,
                    addressLine1 = "Atatürk Cad. No:5",
                    town = "Sinop"
                ),
                connections = listOf(
                    ConnectionDto(
                        connectionType = ConnectionTypeDto(title = "Type 2"),
                        powerKw = 22.0,
                        quantity = 2,
                        level = LevelDto(title = "Level 2: Medium", isFastChargeCapable = false)
                    )
                ),
                operatorInfo = OperatorInfoDto(title = "ZES"),
                statusType = StatusTypeDto(title = "Operational", isOperational = true),
                numberOfPoints = 2
            ),
            PoiDto(
                id = 2,
                addressInfo = AddressInfoDto(
                    title = "Sahil Yolu Hızlı Şarj",
                    latitude = lat - 0.015,
                    longitude = lng + 0.008,
                    addressLine1 = "Sahil Cad. No:12",
                    town = "Sinop"
                ),
                connections = listOf(
                    ConnectionDto(
                        connectionType = ConnectionTypeDto(title = "CCS"),
                        powerKw = 50.0,
                        quantity = 1,
                        level = LevelDto(title = "Level 3: High", isFastChargeCapable = true)
                    )
                ),
                operatorInfo = OperatorInfoDto(title = "Eşarj"),
                statusType = StatusTypeDto(title = "Operational", isOperational = true),
                numberOfPoints = 1
            ),
            PoiDto(
                id = 3,
                addressInfo = AddressInfoDto(
                    title = "Otopark Şarj Noktası",
                    latitude = lat + 0.005,
                    longitude = lng - 0.012,
                    addressLine1 = "Cumhuriyet Mah.",
                    town = "Sinop"
                ),
                connections = listOf(
                    ConnectionDto(
                        connectionType = ConnectionTypeDto(title = "Type 2"),
                        powerKw = 11.0,
                        quantity = 4,
                        level = LevelDto(title = "Level 2: Medium", isFastChargeCapable = false)
                    )
                ),
                operatorInfo = OperatorInfoDto(title = "Voltrun"),
                statusType = StatusTypeDto(title = "Faulty", isOperational = false),
                numberOfPoints = 4
            )
        )
    }

    // DTO -> Domain Model dönüşümü. Bu fonksiyon, gerçek API'ye geçince de
    // aynen kullanılacak, çünkü gerçek API de aynı PoiDto formatında veri döndürüyor.
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