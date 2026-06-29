package com.kadirdeliceli.chargefinder.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kadirdeliceli.chargefinder.data.repository.StationRepository
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StationsUiState {
    data object Loading : StationsUiState
    data class Success(val stations: List<ChargingStation>) : StationsUiState
    data class Error(val message: String) : StationsUiState
}

class MapViewModel(
    private val repository: StationRepository = StationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<StationsUiState>(StationsUiState.Loading)
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    private val _selectedStation = MutableStateFlow<ChargingStation?>(null)
    val selectedStation: StateFlow<ChargingStation?> = _selectedStation.asStateFlow()

    // Filtre durumu
    enum class StationFilter { ALL, FAST, AVAILABLE }

    private val _selectedFilter = MutableStateFlow(StationFilter.ALL)
    val selectedFilter: StateFlow<StationFilter> = _selectedFilter.asStateFlow()

    private val _allStations = MutableStateFlow<List<ChargingStation>>(emptyList())

    fun setFilter(filter: StationFilter) {
        _selectedFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val all = _allStations.value
        val filtered = when (_selectedFilter.value) {
            StationFilter.ALL -> all
            StationFilter.FAST -> all.filter { station ->
                station.connectors.any { it.isFastCharge }
            }
            StationFilter.AVAILABLE -> all.filter { it.isOperational }
        }
        _uiState.value = StationsUiState.Success(filtered)
    }

    fun loadStations(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = StationsUiState.Loading
            try {
                val stations = repository.getNearbyStations(latitude, longitude)
                    .map { station ->
                        station.copy(
                            distanceKm = calculateDistanceKm(
                                latitude, longitude,
                                station.latitude, station.longitude
                            )
                        )
                    }
                    .sortedBy { it.distanceKm }
                _allStations.value = stations
                applyFilter()
            } catch (e: Exception) {
                _uiState.value = StationsUiState.Error(
                    e.message ?: "İstasyonlar yüklenirken bir hata oluştu"
                )
            }
        }
    }

    fun onStationSelected(station: ChargingStation) {
        _selectedStation.value = station
    }

    fun onStationDetailDismissed() {
        _selectedStation.value = null
    }

    // İki coğrafi nokta arası mesafeyi km cinsinden hesaplar
    private fun calculateDistanceKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0] / 1000.0
    }
}