import services.AuthService
import services.CategoriaService
import services.EstadisticaService
import services.ListaCompraService
import services.ProductoService
import services.PromocionService
import utils.ConsoleUtils
import views.AdminView
import views.AuthView
import views.CategoriaView
import views.ListaCompraView
import views.ProductoView
import views.PromocionView

fun main() {
    val consoleUtils = ConsoleUtils()
    val authService = AuthService()
    val categoriaService = CategoriaService()
    val productoService = ProductoService()
    val promocionService = PromocionService()
    val listaCompraService = ListaCompraService(productoService)
    val estadisticaService = EstadisticaService(authService, productoService, promocionService, categoriaService, listaCompraService)

    val categoriaView = CategoriaView(categoriaService, consoleUtils)
    val productoView = ProductoView(productoService, categoriaService, consoleUtils)
    val promocionView = PromocionView(promocionService, productoService, consoleUtils)
    val authView = AuthView(authService, consoleUtils)
    val listaCompraView = ListaCompraView(listaCompraService, productoService, consoleUtils)

    categoriaService.cargarCategoriasIniciales()
    productoService.cargarProductosIniciales(categoriaService)

    val adminView = AdminView(
        categoriaView = categoriaView,
        productoView = productoView,
        promocionView = promocionView,
        authService = authService,
        consoleUtils = consoleUtils,
        estadisticaService = estadisticaService
    )

    var continuar = true

    while (continuar) {

        println()
        println("================================")
        println("         SUPER SELECTOS")
        println("================================")
        println("1. Registrarse")
        println("2. Iniciar sesión")
        println("3. Salir")

        val opcion = consoleUtils.leerEntero("Seleccione una opción: ")

        when (opcion) {
            1 -> authView.registrarCliente()

            2 -> {
                val inicioExitoso = authView.iniciarSesion()
                if (inicioExitoso) {
                    mostrarMenuUsuario(authService, adminView, listaCompraView, consoleUtils, estadisticaService)
                }
            }

            3 -> {
                println("\nGracias por utilizar Super Selectos.")
                continuar = false
            }

            else -> println("\nOpción inválida.")
        }
    }
}

fun mostrarMenuUsuario(
    authService: AuthService,
    adminView: AdminView,
    listaCompraView: ListaCompraView,
    consoleUtils: ConsoleUtils,
    estadisticaService: EstadisticaService
) {
    var cerrar = false

    while (!cerrar) {
        val usuario = authService.obtenerUsuarioActual()

        if (usuario == null) {
            cerrar = true
            continue
        }

        usuario.mostrarMenu()

        when (usuario.rol) {
            "CLIENTE" -> {
                val opcion = consoleUtils.leerTexto("Seleccione una opción: ")

                when (opcion) {
                    "1" -> listaCompraView.menuListaCompra()
                    "2" -> mostrarEstadisticasCliente(estadisticaService)
                    "3" -> {
                        authService.cerrarSesion()
                        cerrar = true
                    }
                    else -> println("Opción inválida.")
                }
            }

            "ADMINISTRADOR" -> {
                adminView.mostrarMenuAdmin()
                //cerrar = true
            }
        }
    }

}

fun mostrarEstadisticasCliente(estadisticaService: EstadisticaService) {
    val totalCompra = estadisticaService.obtenerTotalCompraCliente()
    val saldoDisponible = estadisticaService.obtenerSaldoDisponibleCliente()
    println("\n==============================================")
    println("      ESTADÍSTICAS DE USUARIO Y COMPRA      ")
    println("==============================================")
    println("  • Productos distintos:           ${estadisticaService.obtenerProductosDistintosCliente()}")
    println("  • Unidades totales a comprar:    ${estadisticaService.obtenerUnidadesTotalesCliente()}")
    println("  • Total de compra:               $${"%.2f".format(totalCompra)}")
    println("  • Saldo disponible:              $${"%.2f".format(saldoDisponible)}")
    println("  • Diferencia de presupuesto:     $${"%.2f".format(saldoDisponible - totalCompra)}")
    println("==============================================")
}
