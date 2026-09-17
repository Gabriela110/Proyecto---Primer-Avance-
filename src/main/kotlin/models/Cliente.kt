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
        println("1. Mis listas de compras")
        println("2. Productos")
        println("3. Promociones")
        println("4. Presupuesto")
        println("5. Estadísticas")
        println("6. Cerrar sesión")
    }
}