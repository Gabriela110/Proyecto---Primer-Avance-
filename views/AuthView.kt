package views

import services.AuthService
import utils.ConsoleUtils

class AuthView(
    private val authService: AuthService,
    private val consoleUtils: ConsoleUtils
) {

    fun registrarCliente() {
        println("\n===== REGISTRO DE CLIENTE =====")

        val nombre = consoleUtils.leerTexto("Nombre: ")
        val correo = consoleUtils.leerTexto("Correo: ")
        val contrasena = consoleUtils.leerTexto("Contraseña: ")

        authService.registrarCliente(
            nombre = nombre,
            correo = correo,
            contrasena = contrasena
        )
    }

    fun iniciarSesion(): Boolean {
        println("\n===== INICIAR SESIÓN =====")

        val correo = consoleUtils.leerTexto("Correo: ")
        val contrasena = consoleUtils.leerTexto("Contraseña: ")

        return authService.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        )
    }
}