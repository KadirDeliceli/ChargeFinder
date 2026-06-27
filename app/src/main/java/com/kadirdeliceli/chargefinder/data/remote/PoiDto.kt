package com.kadirdeliceli.chargefinder.data.remote

import com.google.gson.annotations.SerializedName

data class PoiDto(
    @SerializedName("ID") val id: Int,
    @SerializedName("AddressInfo") val addressInfo: AddressInfoDto?,
    @SerializedName("Connections") val connections: List<ConnectionDto>?,
    @SerializedName("OperatorInfo") val operatorInfo: OperatorInfoDto?,
    @SerializedName("StatusType") val statusType: StatusTypeDto?,
    @SerializedName("NumberOfPoints") val numberOfPoints: Int?
)

data class AddressInfoDto(
    @SerializedName("Title") val title: String?,
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("AddressLine1") val addressLine1: String?,
    @SerializedName("Town") val town: String?
)

data class ConnectionDto(
    @SerializedName("ConnectionType") val connectionType: ConnectionTypeDto?,
    @SerializedName("PowerKW") val powerKw: Double?,
    @SerializedName("Quantity") val quantity: Int?,
    @SerializedName("Level") val level: LevelDto?
)

data class ConnectionTypeDto(
    @SerializedName("Title") val title: String?
)

data class LevelDto(
    @SerializedName("Title") val title: String?,
    @SerializedName("IsFastChargeCapable") val isFastChargeCapable: Boolean?
)

data class OperatorInfoDto(
    @SerializedName("Title") val title: String?
)

data class StatusTypeDto(
    @SerializedName("Title") val title: String?,
    @SerializedName("IsOperational") val isOperational: Boolean?
)