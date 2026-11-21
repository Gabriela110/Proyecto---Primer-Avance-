package utils

data class AuthErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirm: String? = null,
    val general: String? = null
) {
    fun hasErrors(): Boolean =
        name != null || email != null || password != null || confirm != null || general != null
}

class ValidationException(val errors: AuthErrors) : Exception("Error de validación")

object AuthValidation {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun validarLogin(email: String, password: String): AuthErrors {
        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !emailRegex.matches(email.trim()) -> "Correo no válido"
            else -> null
        }

        val passwordError = if (password.isBlank()) {
            "La contraseña es obligatoria"
        } else {
            null
        }

        return AuthErrors(
            email = emailError,
            password = passwordError
        )
    }

    fun validarRegistro(
        name: String,
        email: String,
        password: String,
        confirm: String
    ): AuthErrors {
        val nameError = if (name.isBlank()) "El nombre es obligatorio" else null

        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !emailRegex.matches(email.trim()) -> "Correo no válido"
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

        return AuthErrors(
            name = nameError,
            email = emailError,
            password = passError,
            confirm = confirmError
        )
    }
}