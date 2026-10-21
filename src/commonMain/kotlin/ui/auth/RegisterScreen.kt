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
fun RegisterScreen(
    authService: AuthService,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(AuthErrors()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro de Cliente", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Nombre
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; errors = errors.copy(name = null) },
            label = { Text("Nombre completo") },
            isError = errors.name != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.name != null) {
            Text(errors.name!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errors = errors.copy(email = null) },
            label = { Text("Correo electrónico") },
            isError = errors.email != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.email != null) {
            Text(errors.email!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errors = errors.copy(password = null) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errors.password != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.password != null) {
            Text(errors.password!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; errors = errors.copy(confirm = null) },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errors.confirm != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errors.confirm != null) {
            Text(errors.confirm!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse
        Button(
            onClick = {
                try {
                    errors = AuthErrors()
                    authService.registrarCliente(name, email, password, confirm)
                    onRegisterSuccess()
                } catch (e: ValidationException) {
                    errors = e.errors
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Regresar al Login
        TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("¿Ya tenés cuenta? Volver al Login")
        }
    }
}