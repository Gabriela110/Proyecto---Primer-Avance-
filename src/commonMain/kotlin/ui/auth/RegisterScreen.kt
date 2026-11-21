
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
fun RegisterScreen(
    firebaseAuthService: FirebaseAuthService,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(AuthErrors()) }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Registro de Cliente",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Nombre
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errors = errors.copy(name = null, general = null)
            },
            label = { Text("Nombre completo") },
            isError = errors.name != null,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        if (errors.name != null) {
            Text(
                errors.name!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Correo
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

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errors = errors.copy(password = null, confirm = null, general = null)
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

        Spacer(modifier = Modifier.height(8.dp))

        // Campo Confirmar Contraseña
        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                errors = errors.copy(confirm = null, general = null)
            },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = errors.confirm != null,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        if (errors.confirm != null) {
            Text(
                errors.confirm!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse
        Button(
            onClick = {
                val nameError = if (name.isBlank()) {
                    "El nombre es obligatorio"
                } else {
                    null
                }

                val emailError = when {
                    email.isBlank() -> "El correo es obligatorio"
                    !email.trim().matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
                    ) -> "Correo no válido"
                    else -> null
                }

                val passwordError = when {
                    password.isBlank() -> "La contraseña es obligatoria"
                    password.length < 6 -> "Mínimo 6 caracteres"
                    else -> null
                }

                val confirmError = when {
                    confirm.isBlank() -> "Confirmá la contraseña"
                    confirm != password -> "Las contraseñas no coinciden"
                    else -> null
                }

                errors = AuthErrors(
                    name = nameError,
                    email = emailError,
                    password = passwordError,
                    confirm = confirmError
                )

                if (!errors.hasErrors()) {
                    scope.launch {
                        cargando = true

                        try {
                            firebaseAuthService.registrar(
                                email = email,
                                password = password,
                                nombre = name,
                                rol = "cliente"
                            )

                            onRegisterSuccess()

                        } catch (e: Exception) {
                            errors = AuthErrors(
                                general = "No se pudo completar el registro. Verificá el correo e intentá nuevamente."
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
            Text(if (cargando) "Registrando..." else "Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Regresar al Login
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            Text("¿Ya tenés cuenta? Volver al Login")
        }

        if (errors.general != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                errors.general!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
