package com.kadirdeliceli.chargefinder.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kadirdeliceli.chargefinder.domain.model.ChargingStation
import com.kadirdeliceli.chargefinder.domain.model.Connector
import kotlinx.coroutines.tasks.await

private val DEFAULT_LOCATION = LatLng(42.0231, 35.1531) // Sinop

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
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            if (uiState is StationsUiState.Success) {
                val stations = (uiState as StationsUiState.Success).stations
                stations.forEach { station ->
                    val markerIcon = remember(station.id) {
                        createStationMarker(
                            color = operatorColor(station.operatorName),
                            isOperational = station.isOperational
                        )
                    }
                    Marker(
                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                        title = station.name,
                        snippet = station.operatorName,
                        icon = markerIcon,
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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is StationsUiState.Error -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (uiState as StationsUiState.Error).message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            is StationsUiState.Success -> { /* marker'lar haritada */ }
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
    val context = LocalContext.current
    val accentColor = Color(operatorColor(station.operatorName))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Üst kısım: operatör rozeti + isim
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    station.operatorName?.let {
                        Text(
                            text = it.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 26.sp
                    )
                }
            }

            // Durum çipi
            Spacer(modifier = Modifier.height(14.dp))
            StatusChip(isOperational = station.isOperational)

            // Adres
            station.address?.let {
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bağlantı noktaları
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Bağlantı Noktaları",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            station.connectors.forEach { connector ->
                ConnectorCard(connector = connector, accentColor = accentColor)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Butonlar
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${station.latitude},${station.longitude}")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallback = Uri.parse("geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, fallback))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yol Tarifi")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* şarj entegrasyonu ileride */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Şarj Et")
            }

            if (!station.isChargingSupported) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Şarj başlatma yakında aktif olacak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(isOperational: Boolean) {
    val bg = if (isOperational) Color(0xFF065F46) else Color(0xFF7F1D1D)
    val dot = if (isOperational) Color(0xFF34D399) else Color(0xFFF87171)
    val label = if (isOperational) "Çalışıyor" else "Arızalı / Kapalı"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = dot,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConnectorCard(connector: Connector, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connector.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = buildString {
                        append("${connector.quantity} adet")
                        if (connector.isFastCharge) append(" • Hızlı şarj")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            connector.powerKw?.let {
                Text(
                    text = "${it.toInt()} kW",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}