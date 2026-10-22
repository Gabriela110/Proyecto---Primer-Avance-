package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import services.AuthService
import services.AuthErrors
import services.ValidationException

@Composable
fun LoginScreen(
    authService: AuthService,
    onLoginSuccess: (Boolean) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(AuthErrors()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("SuperApp", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Controlá tu presupuesto y tus compras",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        // Campo de Correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errors = errors.copy(email = null, general = null) },
            label = { Text("Correo electrónico") },
            isError = errors.email != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.email != null) {
            Text(errors.email!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errors = errors.copy(password = null, general = null) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errors.password != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.password != null) {
            Text(errors.password!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Error general de credenciales incorrectas
        if (errors.general != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errors.general!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Inicio de Sesión
        Button(
            onClick = {
                try {
                    errors = AuthErrors()
                    val usuario = authService.iniciarSesion(email, password)
                    val esAdmin = authService.esAdministradorActual()
                    onLoginSuccess(esAdmin)
                } catch (e: ValidationException) {
                    errors = e.errors
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para ir al registro
        TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("¿No tenés cuenta? Registrate aquí")
        }
    }
}