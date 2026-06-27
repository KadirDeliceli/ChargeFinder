package com.kadirdeliceli.chargefinder.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OcmApiService {

    @GET("poi/")
    suspend fun getNearbyStations(
        @Query("key") apiKey: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distance: Int = 10,
        @Query("distanceunit") distanceUnit: String = "km",
        @Query("maxresults") maxResults: Int = 100,
        @Query("compact") compact: Boolean = true,
        @Query("verbose") verbose: Boolean = false
    ): List<PoiDto>
}