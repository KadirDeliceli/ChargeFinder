package com.kadirdeliceli.chargefinder.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kadirdeliceli.chargefinder.ui.auth.ProfileScreen
import com.kadirdeliceli.chargefinder.ui.map.MapScreen
import com.kadirdeliceli.chargefinder.ui.qr.QrScreen
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp

sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Map : BottomTab("tab_map", "Harita", Icons.Filled.Map)
    data object Qr : BottomTab("tab_qr", "QR", Icons.Filled.QrCodeScanner)
    data object Profile : BottomTab("tab_profile", "Profil", Icons.Filled.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val tabNavController = rememberNavController()
    val tabs = listOf(BottomTab.Map, BottomTab.Qr, BottomTab.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.Map.route) { MapScreen() }
            composable(BottomTab.Qr.route) { QrScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}