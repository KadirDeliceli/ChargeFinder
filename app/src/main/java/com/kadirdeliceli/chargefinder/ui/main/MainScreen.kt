package com.kadirdeliceli.chargefinder.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kadirdeliceli.chargefinder.ui.auth.ProfileScreen
import com.kadirdeliceli.chargefinder.ui.map.MapScreen
import com.kadirdeliceli.chargefinder.ui.qr.QrScreen

// Bottom bar'daki sekmeleri tanımlıyoruz
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
            NavigationBar {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                // Aynı sekmeye tekrar basınca stack şişmesin
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
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
            composable(BottomTab.Map.route) {
                MapScreen()
            }
            composable(BottomTab.Qr.route) {
                QrScreen()
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}