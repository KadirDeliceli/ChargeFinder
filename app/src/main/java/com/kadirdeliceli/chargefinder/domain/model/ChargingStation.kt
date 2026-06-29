package com.kadirdeliceli.chargefinder.domain.model

data class ChargingStation(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val operatorName: String?,
    val isOperational: Boolean,
    val totalPoints: Int,
    val connectors: List<Connector>,
    val isChargingSupported: Boolean = false,
    val distanceKm: Double? = null
)

data class Connector(
    val type: String,
    val powerKw: Double?,
    val quantity: Int,
    val isFastCharge: Boolean
)