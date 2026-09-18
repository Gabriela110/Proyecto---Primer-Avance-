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
        println("===== MENÚ ADMINISTRADOR =====")
        println("Bienvenido/a, $nombre")
        println("1. Gestionar productos")
        println("2. Gestionar promociones")
        println("3. Ver estadísticas")
        println("4. Ver usuarios")
        println("5. Cerrar sesión")
    }
}