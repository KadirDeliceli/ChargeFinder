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

    fun loadStations(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = StationsUiState.Loading
            try {
                val stations = repository.getNearbyStations(latitude, longitude)
                _uiState.value = StationsUiState.Success(stations)
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
}