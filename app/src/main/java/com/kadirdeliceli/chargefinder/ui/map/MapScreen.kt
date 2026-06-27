package com.kadirdeliceli.chargefinder.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.tasks.await

private val DEFAULT_LOCATION = LatLng(42.0231, 35.1531) // Sinop, izin verilmezse / konum alınamazsa kullanılacak

@Composable
fun MapScreen(
    viewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var userLocation by remember { mutableStateOf(DEFAULT_LOCATION) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                val location = fusedClient.lastLocation.await()
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (e: SecurityException) {
                // izin yok, varsayılan konum kullanılacak
            }
            viewModel.loadStations(userLocation.latitude, userLocation.longitude)
        } else {
            viewModel.loadStations(userLocation.latitude, userLocation.longitude)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = com.google.maps.android.compose.MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            if (uiState is StationsUiState.Success) {
                val stations = (uiState as StationsUiState.Success).stations
                stations.forEach { station ->
                    Marker(
                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                        title = station.name,
                        snippet = station.operatorName,
                        onClick = {
                            viewModel.onStationSelected(station)
                            true
                        }
                    )
                }
            }
        }

        when (uiState) {
            is StationsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
            is StationsUiState.Error -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = (uiState as StationsUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            is StationsUiState.Success -> { /* marker'lar zaten haritada çiziliyor */ }
        }
    }

    selectedStation?.let { station ->
        StationDetailBottomSheet(
            station = station,
            onDismiss = { viewModel.onStationDetailDismissed() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailBottomSheet(
    station: ChargingStation,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.headlineSmall
            )
            station.operatorName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            station.address?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = if (station.isOperational) "Durum: Çalışıyor" else "Durum: Arızalı/Kapalı",
                style = MaterialTheme.typography.bodyMedium,
                color = if (station.isOperational)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Bağlantı Noktaları",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(station.connectors) { connector ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "${connector.type} ${if (connector.powerKw != null) "- ${connector.powerKw}kW" else ""}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Adet: ${connector.quantity}" + if (connector.isFastCharge) " (Hızlı Şarj)" else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = {
                    // Gerçek şarj entegrasyonu henüz yok, bu yüzden bilgilendirici mesaj gösteriyoruz.
                    // İleride firma API entegrasyonu eklenince, station.isChargingSupported true olacak
                    // ve burada gerçek şarj başlatma akışına yönlendirilecek.
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null)
                Text(
                    text = "Şarj Et",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (!station.isChargingSupported) {
                Text(
                    text = "Bu özellik üzerinde çalışıyoruz, yakında! 🔌",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}