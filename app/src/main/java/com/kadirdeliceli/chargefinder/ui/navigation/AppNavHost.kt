package com.kadirdeliceli.chargefinder.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kadirdeliceli.chargefinder.ui.map.MapScreen

object Routes {
    const val MAP = "map"
    const val PROFILE = "profile"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAP,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.MAP) {
            MapScreen()
        }
        composable(Routes.PROFILE) {
            ProfileScreenPlaceholder(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun ProfileScreenPlaceholder(onBack: () -> Unit) {
    androidx.compose.foundation.layout.Column {
        androidx.compose.material3.Text("Profil Ekranı")
        androidx.compose.material3.Button(onClick = onBack) {
            androidx.compose.material3.Text("Geri Dön")
        }
    }
}