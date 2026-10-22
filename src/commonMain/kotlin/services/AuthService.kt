package services

import models.Administrador
import models.Autenticable
import models.Cliente
import models.Usuario

// Estructura exacta para conservar todos los errores detallados por campo
data class AuthErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirm: String? = null,
    val general: String? = null
) {
    fun hasErrors(): Boolean = name != null || email != null || password != null || confirm != null || general != null
}

// Excepción personalizada para atrapar y mostrar todos los mensajes detallados en la UI
class ValidationException(val errors: AuthErrors) : Exception("Error de validación")

class AuthService {

    private val usuarios = mutableListOf<Usuario>()
    private var usuarioActual: Usuario? = null

    init {
        // El administrador por defecto del sistema
        var adminInicial = Administrador(
            id = 1,
            nombre = "Administrador",
            correo = "admin@superselectos.com",
            contrasena = "admin123"
        )
        var clienteInicial = Cliente(
            id = 2,
            nombre = "Juan Perez",
            correo = "cliente@correo.com",
            contrasena = "cliente123"
        )
        usuarios.addAll(listOf(adminInicial, clienteInicial))
    }

    private fun isValidEmail(email: String): Boolean {
        val emailReference = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailReference.matches(email)
    }

    // Registro con TODAS las validaciones originales intactas
    fun registrarCliente(
        name: String,
        email: String,
        password: String,
        confirm: String
    ): Boolean {
        val nameError = if (name.isBlank()) "El nombre es obligatorio" else null

        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !isValidEmail(email.trim()) -> "Correo no válido"
            usuarios.any { it.correo.equals(email.trim(), ignoreCase = true) } -> "Ese correo ya está registrado"
            else -> null
        }

        val passError = when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        // 👈 Conservado exactamente como lo tenías
        val confirmError = when {
            confirm.isBlank() -> "Confirmá la contraseña"
            confirm != password -> "Las contraseñas no coinciden"
            else -> null
        }

        val errors = AuthErrors(
            name = nameError,
            email = emailError,
            password = passError,
            confirm = confirmError
        )

        if (errors.hasErrors()) {
            throw ValidationException(errors)
        }

        val nuevoId = if (usuarios.isEmpty()) {
            1
        } else {
            usuarios.maxOf { it.id } + 1
        }

        val nuevoCliente = Cliente(
            id = nuevoId,
            nombre = name.trim(),
            correo = email.trim(),
            contrasena = password
        )

        usuarios.add(nuevoCliente)
        return true
    }

    // Inicio de sesión con TODAS las validaciones y mensajes originales intactos
    fun iniciarSesion(email: String, password: String): Usuario {
        val emailError = when {
            email.isBlank() -> "El correo es obligatorio"
            !isValidEmail(email.trim()) -> "Correo no válido"
            else -> null
        }
        val passError = if (password.isBlank()) "La contraseña es obligatoria" else null

        if (emailError != null || passError != null) {
            throw ValidationException(AuthErrors(email = emailError, password = passError))
        }

        val usuario = usuarios.find {
            it.correo.equals(email.trim(), ignoreCase = true)
        }

        if (usuario == null || !usuario.validarContrasena(password)) {
            throw ValidationException(AuthErrors(general = "Correo o contraseña incorrectos"))
        }

        usuarioActual = usuario

        if (usuario is Autenticable) {
            usuario.iniciarSesion()
        }

        return usuario
    }

    fun obtenerUsuarioActual(): Usuario? = usuarioActual

    fun cerrarSesion() {
        val usuario = usuarioActual
        if (usuario is Autenticable) {
            usuario.cerrarSesion()
        }
        usuarioActual = null
    }

    fun obtenerUsuarios(): MutableList<Usuario> {
        return usuarios
    }

    fun haySesionActiva(): Boolean = usuarioActual != null

    fun esAdministradorActual(): Boolean = usuarioActual is Administrador

    fun listarUsuarios() {
        println()
        println("===== USUARIOS REGISTRADOS =====")
        if (usuarios.isEmpty()) {
            println("No hay usuarios registrados.")
            return
        }
        usuarios.forEach { println(it) }
    }
}