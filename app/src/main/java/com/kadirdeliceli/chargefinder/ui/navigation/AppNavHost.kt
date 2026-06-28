package com.kadirdeliceli.chargefinder.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kadirdeliceli.chargefinder.ui.auth.AuthViewModel
import com.kadirdeliceli.chargefinder.ui.auth.LoginScreen
import com.kadirdeliceli.chargefinder.ui.auth.RegisterScreen
import com.kadirdeliceli.chargefinder.ui.map.MapScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAP = "map"
    const val PROFILE = "profile"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    // Uygulama açılışında kullanıcı giriş yapmış mı? Ona göre başlangıç ekranı belirleniyor.
    val authViewModel: AuthViewModel = viewModel()
    val startDestination = if (authViewModel.isUserLoggedIn()) Routes.MAP else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAP) {
                        // Login ekranını geri yığınından temizle
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.MAP) {
                        // Tüm auth ekranlarını geri yığınından temizle
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

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