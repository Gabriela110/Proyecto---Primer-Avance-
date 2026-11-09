package views

import services.AuthService
import services.EstadisticaService

import utils.ConsoleUtils


class AdminView(
    private val categoriaView: CategoriaView,
    private val productoView: ProductoView,
    private val promocionView: PromocionView,
    private val authService: AuthService,
    private val consoleUtils: ConsoleUtils,
    private val estadisticaService: EstadisticaService
) {

    fun mostrarMenuAdmin() {
        var opcion: Int

        do {
            println("\n========== SISTEMA DE GESTIÓN ADMINISTRATIVA ==========")
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
                4 -> mostrarEstadisticas()
                5 -> authService.listarUsuarios()
                6 -> {
                    println("\nCerrando sesión de administrador...")
                    authService.cerrarSesion()
                }
                else -> println("\nOpción inválida.")
            }

        } while (opcion != 6)
    }
    private fun mostrarEstadisticas() {
        println("\n==============================================")
        println("          ESTADÍSTICAS DEL SISTEMA            ")
        println("==============================================")
        println("  • Categorías registradas: ${estadisticaService.obtenerTotalCategorias()}")
        println("  • Productos en catálogo:  ${estadisticaService.obtenerTotalProductos()}")
        println("  • Promociones activas:    ${estadisticaService.obtenerTotalPromociones()}")
        println("  • Usuarios registrados:   ${estadisticaService.obtenerTotalUsuarios()}")
        println("==============================================")
    }
}