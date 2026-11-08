
package ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import services.FirebaseAuthService
import services.AuthErrors

@Composable
fun LoginScreen(
    firebaseAuthService: FirebaseAuthService,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(AuthErrors()) }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errors = errors.copy(email = null, general = null)
            },
            label = { Text("Correo electrónico") },
            isError = errors.email != null,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        if (errors.email != null) {
            Text(
                errors.email!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errors = errors.copy(password = null, general = null)
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errors.password != null,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        if (errors.password != null) {
            Text(
                errors.password!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (errors.general != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                errors.general!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val emailError = when {
                    email.isBlank() -> "El correo es obligatorio"
                    !email.trim().matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
                    ) -> "Correo no válido"
                    else -> null
                }

                val passwordError = if (password.isBlank()) {
                    "La contraseña es obligatoria"
                } else {
                    null
                }

                errors = AuthErrors(
                    email = emailError,
                    password = passwordError
                )

                if (emailError == null && passwordError == null) {
                    scope.launch {
                        cargando = true

                        try {
                            // El rol se determinará al integrar la información
                            // de usuarios con el módulo de Firestore.
                            onLoginSuccess(firebaseAuthService.iniciarSesion(email, password))
                        } catch (e: Exception) {
                            errors = AuthErrors(
                                general = "No se pudo iniciar sesión. Verificá tu correo y contraseña."
                            )
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text(if (cargando) "Ingresando..." else "Ingresar")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text("¿No tenés cuenta? Registrate aquí")
        }
    }
}
