package com.kadirdeliceli.chargefinder.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kadirdeliceli.chargefinder.ui.auth.ProfileScreen
import com.kadirdeliceli.chargefinder.ui.map.MapScreen
import com.kadirdeliceli.chargefinder.ui.qr.QrScreen

object TabRoutes {
    const val MAP = "tab_map"
    const val QR = "tab_qr"
    const val PROFILE = "tab_profile"
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val tabNavController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        // İçerik (sekmeler)
        NavHost(
            navController = tabNavController,
            startDestination = TabRoutes.MAP,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(TabRoutes.MAP) { MapScreen() }
            composable(TabRoutes.QR) { QrScreen() }
            composable(TabRoutes.PROFILE) { ProfileScreen(onLogout = onLogout) }
        }

        // Özel bottom bar (içeriğin üstüne, ekranın altına sabit)
        CustomBottomBar(
            navController = tabNavController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CustomBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun isSelected(route: String): Boolean =
        currentDestination?.hierarchy?.any { it.route == route } == true

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        // Bar yüzeyi
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: Harita
            BottomBarTab(
                icon = Icons.Filled.Map,
                label = "Harita",
                selected = isSelected(TabRoutes.MAP),
                onClick = { navigateTo(TabRoutes.MAP) },
                modifier = Modifier.weight(1f)
            )

            // Orta: QR butonu için boşluk bırak (FAB üstte duracak)
            Box(modifier = Modifier.weight(1f))

            // Sağ: Profil
            BottomBarTab(
                icon = Icons.Filled.Person,
                label = "Profil",
                selected = isSelected(TabRoutes.PROFILE),
                onClick = { navigateTo(TabRoutes.PROFILE) },
                modifier = Modifier.weight(1f)
            )
        }

        // Ortadaki havada duran QR butonu
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (isSelected(TabRoutes.QR))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navigateTo(TabRoutes.QR) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = "QR",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun BottomBarTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}