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
        delay(800)
        return getMockPois(latitude, longitude).map { it.toDomainModel() }
    }

    private fun getMockPois(lat: Double, lng: Double): List<PoiDto> {
        // Yardımcı: tek tip soketli istasyon üretmeyi kolaylaştırır
        var idCounter = 1
        fun station(
            name: String,
            latOffset: Double,
            lngOffset: Double,
            operator: String,
            address: String,
            isOperational: Boolean,
            connectorType: String,
            powerKw: Double,
            quantity: Int,
            isFast: Boolean
        ): PoiDto {
            return PoiDto(
                id = idCounter++,
                addressInfo = AddressInfoDto(
                    title = name,
                    latitude = lat + latOffset,
                    longitude = lng + lngOffset,
                    addressLine1 = address,
                    town = "Sinop"
                ),
                connections = listOf(
                    ConnectionDto(
                        connectionType = ConnectionTypeDto(title = connectorType),
                        powerKw = powerKw,
                        quantity = quantity,
                        level = LevelDto(
                            title = if (isFast) "Level 3: High" else "Level 2: Medium",
                            isFastChargeCapable = isFast
                        )
                    )
                ),
                operatorInfo = OperatorInfoDto(title = operator),
                statusType = StatusTypeDto(
                    title = if (isOperational) "Operational" else "Faulty",
                    isOperational = isOperational
                ),
                numberOfPoints = quantity
            )
        }

        return listOf(
            // ZES (3 adet)
            station("ZES Migros AVM", 0.012, 0.010, "ZES", "Atatürk Cad. No:5", true, "Type 2", 22.0, 2, false),
            station("ZES Sahil Park", -0.008, 0.018, "ZES", "Sahil Cad. No:40", true, "CCS", 60.0, 1, true),
            station("ZES Otogar", 0.020, -0.006, "ZES", "Otogar Karşısı", false, "Type 2", 22.0, 3, false),

            // Eşarj (3 adet)
            station("Eşarj Sahil Hızlı", -0.015, 0.008, "Eşarj", "Sahil Cad. No:12", true, "CCS", 50.0, 1, true),
            station("Eşarj Şehir Merkezi", 0.006, 0.014, "Eşarj", "Cumhuriyet Meydanı", true, "Type 2", 22.0, 2, false),
            station("Eşarj Üniversite", 0.025, 0.020, "Eşarj", "Üniversite Kampüsü", true, "CHAdeMO", 50.0, 1, true),

            // Voltrun (3 adet)
            station("Voltrun Otopark", 0.005, -0.012, "Voltrun", "Cumhuriyet Mah.", true, "Type 2", 11.0, 4, false),
            station("Voltrun Marina", -0.018, -0.004, "Voltrun", "Marina Girişi", true, "CCS", 90.0, 2, true),
            station("Voltrun AVM", 0.014, -0.018, "Voltrun", "Park AVM Otopark", false, "Type 2", 22.0, 2, false),

            // Trugo (3 adet)
            station("Trugo Benzinlik", -0.006, 0.022, "Trugo", "D010 Karayolu", true, "CCS", 120.0, 2, true),
            station("Trugo Şehir", 0.018, 0.004, "Trugo", "İnönü Cad. No:8", true, "Type 2", 22.0, 3, false),
            station("Trugo Sahil", -0.022, 0.012, "Trugo", "Sahil Yolu No:60", true, "CCS", 150.0, 1, true),

            // Astor (2 adet)
            station("Astor Plaza", 0.009, 0.025, "Astor", "Plaza Otopark", true, "Type 2", 22.0, 2, false),
            station("Astor Sanayi", -0.012, -0.016, "Astor", "Sanayi Sitesi", true, "CCS", 60.0, 1, true),

            // Sharz (2 adet)
            station("Sharz Hastane", 0.022, -0.014, "Sharz", "Devlet Hastanesi", true, "Type 2", 11.0, 2, false),
            station("Sharz Belediye", -0.004, -0.020, "Sharz", "Belediye Önü", true, "CCS", 50.0, 1, true),

            // Beefull (2 adet)
            station("Beefull Market", 0.016, 0.016, "Beefull", "Market Otoparkı", true, "Type 2", 22.0, 3, false),
            station("Beefull Köprü", -0.020, 0.020, "Beefull", "Köprübaşı Mevkii", false, "CCS", 50.0, 1, true),

            // Bilinmeyen operatör (2 adet - rengi isimden türetilecek)
            station("Şehir Otoparkı Şarj", 0.003, -0.008, "EnerjiSA", "Merkez Otopark", true, "Type 2", 22.0, 2, false),
            station("Liman Şarj Noktası", -0.010, 0.006, "PetrolOfisi", "Liman Girişi", true, "CCS", 75.0, 1, true)
        )
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