package models

class Cliente(
    id: Int,
    nombre: String,
    correo: String,
    contrasena: String
) : Usuario(id, nombre, correo, contrasena), Autenticable {

    override val rol: String = "CLIENTE"

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
        println("===== MENÚ CLIENTE =====")
        println("Bienvenido/a, $nombre")
        println("1. Gestión de presupuesto y compras")
        println("2. Estadísticas")
        println("3. Cerrar sesión")
    }
}