
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

import services.AuthService
import services.FirebaseAuthService
import services.CategoriaService
import services.ListaCompraService
import services.ProductoService
import services.PromocionService

import ui.Screen
import ui.theme.SuperAppTheme
import ui.auth.LoginScreen
import ui.auth.RegisterScreen
import ui.admin.AdminDashboardScreen
import ui.admin.UsuariosScreen
import ui.client.CatalogoScreen
import ui.client.ClientTab
import ui.client.MasScreen
import ui.client.PromocionesScreen
import ui.admin.ProductManagementScreen
import ui.admin.CategoryManagementScreen
import ui.client.ShoppingListScreen

// TEMPORAL: el login de Firebase todavía no trae el rol del usuario (queda pendiente con Firestore).
// Mientras tanto, la cuenta con este correo entra como administrador y las demás como clientes.
// Para probar el panel de administración, registrá esta cuenta desde la pantalla de registro.
private const val CORREO_ADMIN = "admin@superselectos.com"

@Composable
fun App() {

    // Instanciamos los servicios principales
    val authService = remember { AuthService() }
    val firebaseAuthService = remember { FirebaseAuthService() }

    val productoService = remember { ProductoService() }
    val categoriaService = remember { CategoriaService() }
    val promocionService = remember { PromocionService() }
    // La lista de compras aplica las promociones vigentes a los precios
    val listaCompraService = remember { ListaCompraService(productoService, promocionService) }

    val scope = rememberCoroutineScope()

    // Carga inicial de información: solo una vez (si el administrador borra todo, no se vuelve a sembrar)
    remember {
        categoriaService.cargarCategoriasIniciales()
        productoService.cargarProductosIniciales(categoriaService)
        promocionService.cargarPromocionesIniciales(productoService)
        true
    }

    MaterialTheme {

        // Pantalla inicial
        var currentScreen by remember {
            mutableStateOf<Screen>(Screen.Login)
        }

        // Pantalla a la que regresan las promociones
        var promosVolverA by remember {
            mutableStateOf<Screen>(Screen.ShoppingList)
        }

        // Navegación de la barra inferior del cliente
        fun irAPestana(tab: ClientTab) {
            when (tab) {
                ClientTab.INICIO,
                ClientTab.LISTAS -> currentScreen = Screen.ShoppingList

                ClientTab.PRODUCTOS -> currentScreen = Screen.Catalog

                ClientTab.PROMOS -> {
                    promosVolverA = Screen.ShoppingList
                    currentScreen = Screen.Promotions
                }

                ClientTab.MAS -> currentScreen = Screen.More
            }
        }

        fun cerrarSesion() {
            scope.launch {
                firebaseAuthService.cerrarSesion()
                currentScreen = Screen.Login
            }
        }

        SuperAppTheme {

            when (currentScreen) {

                // LOGIN
                is Screen.Login -> {
                    LoginScreen(
                        firebaseAuthService = firebaseAuthService,
                        onLoginSuccess = { _ ->
                            val esAdministrador = firebaseAuthService.obtenerCorreoActual()
                                ?.trim().equals(CORREO_ADMIN, ignoreCase = true)
                            currentScreen = if (esAdministrador) {
                                Screen.AdminDashboard
                            } else {
                                Screen.ShoppingList
                            }
                        },
                        onNavigateToRegister = {
                            currentScreen = Screen.Register
                        }
                    )
                }

                // REGISTRO
                is Screen.Register -> {
                    RegisterScreen(
                        firebaseAuthService = firebaseAuthService,
                        onRegisterSuccess = {
                            scope.launch {
                                firebaseAuthService.cerrarSesion()
                                currentScreen = Screen.Login
                            }
                        },
                        onBackToLogin = {
                            currentScreen = Screen.Login
                        }
                    )
                }

                // PANEL ADMINISTRADOR
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        authService = authService,
                        onNavigateToProductos = {
                            currentScreen = Screen.Products
                        },
                        onNavigateToPromociones = {
                            promosVolverA = Screen.AdminDashboard
                            currentScreen = Screen.Promotions
                        },
                        onNavigateToCategorias = {
                            currentScreen = Screen.Categories
                        },
                        onNavigateToUsuarios = {
                            currentScreen = Screen.Users
                        },
                        onLogout = { cerrarSesion() }
                    )
                }

                // LISTA DE COMPRAS (cliente)
                is Screen.ShoppingList -> {
                    ShoppingListScreen(
                        listaCompraService = listaCompraService,
                        onNavigate = { tab -> irAPestana(tab) }
                    )
                }

                // CATÁLOGO DE PRODUCTOS (cliente)
                is Screen.Catalog -> {
                    CatalogoScreen(
                        productoService = productoService,
                        listaCompraService = listaCompraService,
                        onNavigate = { tab -> irAPestana(tab) }
                    )
                }

                // MÁS (cliente)
                is Screen.More -> {
                    MasScreen(
                        correo = firebaseAuthService.obtenerCorreoActual(),
                        onNavigate = { tab -> irAPestana(tab) },
                        onLogout = { cerrarSesion() }
                    )
                }

                // USUARIOS
                is Screen.Users -> {
                    UsuariosScreen(
                        authService = authService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        }
                    )
                }

                // PRODUCTOS (administrador)
                is Screen.Products -> {
                    ProductManagementScreen(
                        productoService = productoService,
                        categoriaService = categoriaService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        },
                        // Editar un producto cambia su hash: se reconstruye el índice de la lista de compras
                        onProductoModificado = { listaCompraService.reindexar() },
                        // Al eliminar un producto también se borran sus promociones y se quita de la lista
                        onProductoEliminado = { producto ->
                            promocionService.eliminarPorProductoId(producto.id)
                            listaCompraService.quitarSiExiste(producto.id)
                        }
                    )
                }

                // CATEGORÍAS (administrador)
                is Screen.Categories -> {
                    CategoryManagementScreen(
                        categoriaService = categoriaService,
                        productoService = productoService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        }
                    )
                }

                // PROMOCIONES
                is Screen.Promotions -> {
                    PromocionesScreen(
                        promocionService = promocionService,
                        productoService = productoService,
                        onBack = {
                            currentScreen = promosVolverA
                        },
                        mostrarBarraInferior =
                            promosVolverA == Screen.ShoppingList,
                        modoAdmin =
                            promosVolverA == Screen.AdminDashboard,
                        onNavigate = { tab -> irAPestana(tab) }
                    )
                }

                // ESTADÍSTICAS
                is Screen.Statistics -> {
                    // Aquí pondremos la vista de Estadísticas
                }

                // RESTO DE PANTALLAS
                else -> {
                    // Pantallas pendientes
                }
            }
        }
    }
}
