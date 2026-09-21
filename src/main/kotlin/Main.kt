import services.AuthService
import services.CategoriaService
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
        consoleUtils = consoleUtils
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
                    // 3. Pasamos listaCompraView a la función del menú
                    mostrarMenuUsuario(authService, adminView, listaCompraView, consoleUtils)
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
    consoleUtils: ConsoleUtils
) {
    var cerrar = false

    while (!cerrar) {
        val usuario = authService.obtenerUsuarioActual()

        if (usuario == null) {
            cerrar = true
            continue
        }

        usuario.mostrarMenu()
        val opcion = consoleUtils.leerTexto("Seleccione una opción: ")

        when (usuario.rol) {
            "CLIENTE" -> {
                when (opcion) {
                    // 4. Delegamos el control a la vista del módulo
                    "1" -> listaCompraView.menuListaCompra()
                    "2" -> println("Módulo de productos.")
                    "3" -> println("Módulo de promociones.")
                    "4" -> println("Módulo de presupuesto.")
                    "5" -> println("Módulo de estadísticas.")
                    "6" -> {
                        authService.cerrarSesion()
                        cerrar = true
                    }
                    else -> println("Opción inválida.")
                }
            }

            "ADMINISTRADOR" -> {
                adminView.mostrarMenuAdmin()
            }
        }
    }
}