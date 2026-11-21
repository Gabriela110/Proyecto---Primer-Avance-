package models

import proyecto_primer_avance.generated.resources.Res

class Cliente(
    id: String,
    nombre: String,
    correo: String,
    rol: String
) : Usuario(id, nombre, correo, rol), Autenticable {

    private var sesionActiva: Boolean = false

    override fun iniciarSesion(): Boolean {
        sesionActiva = true
        return sesionActiva
    }

    override fun cerrarSesion() {
        sesionActiva = false
        println("Sesión cerrada correctamente.")
    }

    /*override fun mostrarMenu() {
        println()
        println("===== MENÚ CLIENTE =====")
        println("Bienvenido/a, $nombre")
        println("1. Gestión de presupuesto y compras")
        println("2. Estadísticas")
        println("3. Cerrar sesión")
    }*/
}