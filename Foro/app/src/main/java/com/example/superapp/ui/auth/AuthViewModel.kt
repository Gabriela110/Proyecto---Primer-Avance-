package com.example.superapp.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class Role { ADMIN, CLIENT }

data class User(
    val name: String,
    val email: String,
    val password: String,
    val role: Role
)

object UserRepository {
    private val users = mutableListOf(
        // Cambiá estas credenciales por las del README del primer avance
        User("Administrador", "admin@superapp.com", "admin123", Role.ADMIN)
    )

    fun findByCredentials(email: String, password: String): User? =
        users.firstOrNull { it.email.equals(email, ignoreCase = true) && it.password == password }

    fun emailExists(email: String): Boolean =
        users.any { it.email.equals(email, ignoreCase = true) }

    fun add(user: User) {
        users.add(user)
    }
}

data class AuthErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirm: String? = null,
    val general: String? = null
)

class AuthViewModel : ViewModel() {

    var errors by mutableStateOf(AuthErrors())
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    fun clearErrors() {
        errors = AuthErrors()
    }

    fun login(email: String, password: String, onSuccess: (Role) -> Unit) {
        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido"
            else -> null
        }
        val passError = if (password.isBlank()) "La contraseña es obligatoria" else null

        if (emailError != null || passError != null) {
            errors = AuthErrors(email = emailError, password = passError)
            return
        }

        val user = UserRepository.findByCredentials(email.trim(), password)
        if (user == null) {
            errors = AuthErrors(general = "Correo o contraseña incorrectos")
        } else {
            errors = AuthErrors()
            currentUser = user
            onSuccess(user.role)
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirm: String,
        onSuccess: () -> Unit
    ) {
        val nameError = if (name.isBlank()) "El nombre es obligatorio" else null

        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido"
            UserRepository.emailExists(email.trim()) -> "Ese correo ya está registrado"
            else -> null
        }

        val passError = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        val confirmError = when {
            confirm.isBlank() -> "Confirmá la contraseña"
            confirm != password -> "Las contraseñas no coinciden"
            else -> null
        }

        if (listOf(nameError, emailError, passError, confirmError).any { it != null }) {
            errors = AuthErrors(nameError, emailError, passError, confirmError)
            return
        }

        UserRepository.add(User(name.trim(), email.trim(), password, Role.CLIENT))
        errors = AuthErrors()
        onSuccess()
    }

    fun logout() {
        currentUser = null
    }
}
