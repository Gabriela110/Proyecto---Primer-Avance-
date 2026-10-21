package ui.auth

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import ui.Screen

@Composable
fun SuperAppNav(
    onNavigateToMain: () -> Unit
) {
    val authViewModel = remember { AuthViewModel() }
    var currentAuthScreen by rememberSaveable { mutableStateOf("login") }

    when (currentAuthScreen) {
        "login" -> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    onNavigateToMain()
                },
                onGoToRegister = {
                    currentAuthScreen = "register"
                }
            )
        }

        "register" -> {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    currentAuthScreen = "login"
                },
                onBackToLogin = {
                    currentAuthScreen = "login"
                }
            )
        }
    }
}