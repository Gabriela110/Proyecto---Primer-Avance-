package services

import models.Administrador
import models.Autenticable
import models.Cliente
import models.Usuario

class AuthService {

    private val usuarios = mutableListOf<Usuario>()

    private var usuarioActual: Usuario? = null

    init {
        // El administrador pertenece al sistema y se registra inicialmente.
        usuarios.add(
            Administrador(
                id = 1,
                nombre = "Administrador",
                correo = "admin@superselectos.com",
                contrasena = "admin123"
            )
        )
    }

    fun registrarCliente(
        nombre: String,
        correo: String,
        contrasena: String
    ): Boolean {

        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            println("Error: todos los campos son obligatorios.")
            return false
        }

        val correoExiste = usuarios.any {
            it.correo.equals(correo, ignoreCase = true)
        }

        if (correoExiste) {
            println("Error: el correo ya está registrado.")
            return false
        }

        val nuevoId = if (usuarios.isEmpty()) {
            1
        } else {
            usuarios.maxOf { it.id } + 1
        }

        val nuevoCliente = Cliente(
            id = nuevoId,
            nombre = nombre,
            correo = correo,
            contrasena = contrasena
        )

        usuarios.add(nuevoCliente)

        println("Cliente registrado correctamente.")
        println("Bienvenido/a, $nombre")

        return true
    }

    fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Boolean {

        val usuario = usuarios.find {
            it.correo.equals(correo, ignoreCase = true)
        }

        if (usuario == null) {
            println("Error: el correo no está registrado.")
            return false
        }

        if (!usuario.validarContrasena(contrasena)) {
            println("Error: contraseña incorrecta.")
            return false
        }

        usuarioActual = usuario

        if (usuario is Autenticable) {
            usuario.iniciarSesion()
        }

        println()
        println("Inicio de sesión exitoso.")
        println("Usuario: ${usuario.nombre}")
        println("Rol: ${usuario.rol}")

        return true
    }

    fun obtenerUsuarioActual(): Usuario? {
        return usuarioActual
    }

    fun cerrarSesion() {

        if (usuarioActual == null) {
            println("No hay ninguna sesión activa.")
            return
        }

        if (usuarioActual is Autenticable) {
            (usuarioActual as Autenticable).cerrarSesion()
        }

        usuarioActual = null
    }

    fun haySesionActiva(): Boolean {
        return usuarioActual != null
    }
}