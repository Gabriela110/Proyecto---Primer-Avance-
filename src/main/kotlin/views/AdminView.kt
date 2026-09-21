package views

import services.AuthService
import utils.ConsoleUtils

class AdminView(
    private val categoriaView: CategoriaView,
    private val productoView: ProductoView,
    private val promocionView: PromocionView,
    private val authService: AuthService,
    private val consoleUtils: ConsoleUtils
) {

    fun mostrarMenuAdmin() {
        var opcion: Int

        do {
            println("\n========== SISTEMA DE PRODUCTOS Y PROMOCIONES ==========")
            println("1. Gestionar productos")
            println("2. Gestionar promociones")
            println("3. Gestionar categorías")
            println("4. Estadísticas del sistema")
            println("5. Ver usuarios registrados")
            println("6. Salir")
            println("==========================================================")

            opcion = consoleUtils.leerEntero("Seleccione una opción: ")

            when (opcion) {
                1 -> productoView.menuProductos()
                2 -> promocionView.menuPromociones()
                3 -> categoriaView.menuCategorias()
                4 -> println("\nEstadísticas del sistema.")
                5 -> authService.listarUsuarios()
                6 -> {
                    println("\nCerrando sesión de administrador...")
                    authService.cerrarSesion()
                }
                else -> println("\nOpción inválida.")
            }

        } while (opcion != 6)
    }
}