package com.example.superapp.ui.auth

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SuperAppNav() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    val destination = if (role == Role.ADMIN) "admin_home" else "client_home"
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.popBackStack("login", inclusive = false)
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Placeholders: sustituir por las pantallas reales
        composable("admin_home") { Text("Inicio administrador") }
        composable("client_home") { Text("Inicio cliente") }
    }
}
