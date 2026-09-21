package models

class Administrador(
    id: Int,
    nombre: String,
    correo: String,
    contrasena: String
) : Usuario(id, nombre, correo, contrasena), Autenticable {

    override val rol: String = "ADMINISTRADOR"

    private var sesionActiva: Boolean = false

    override fun iniciarSesion(): Boolean {
        sesionActiva = true
        return sesionActiva
    }

    override fun cerrarSesion() {
        sesionActiva = false
        println("Sesión cerrada correctamente.")
    }

    override fun mostrarMenu() {
        println()
        println("Bienvenido/a, $nombre")
    }
}