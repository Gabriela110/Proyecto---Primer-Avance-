
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
import ui.client.ClientTab
import ui.client.PromocionesScreen
import ui.admin.ProductManagementScreen
import ui.admin.CategoryManagementScreen
import ui.client.ShoppingListScreen

@Composable
fun App() {

    // Instanciamos los servicios principales
    val authService = remember { AuthService() }
    val firebaseAuthService = remember { FirebaseAuthService() }

    val productoService = remember { ProductoService() }
    val listaCompraService = remember { ListaCompraService(productoService) }
    val categoriaService = remember { CategoriaService() }
    val promocionService = remember { PromocionService() }

    val scope = rememberCoroutineScope()

    // Carga inicial de información
    categoriaService.cargarCategoriasIniciales()
    productoService.cargarProductosIniciales(categoriaService)
    promocionService.cargarPromocionesIniciales(productoService)

    MaterialTheme {

        // Pantalla inicial
        var currentScreen by remember {
            mutableStateOf<Screen>(Screen.Login)
        }

        // Pantalla a la que regresan las promociones
        var promosVolverA by remember {
            mutableStateOf<Screen>(Screen.ShoppingList)
        }

        SuperAppTheme {

            when (currentScreen) {

                // LOGIN
                is Screen.Login -> {
                    LoginScreen(
                        firebaseAuthService = firebaseAuthService,
                        onLoginSuccess = { esAdministrador ->
                            if (esAdministrador) {
                                currentScreen = Screen.AdminDashboard
                            } else {
                                currentScreen = Screen.ShoppingList
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
                        onLogout = {
                            scope.launch {
                                firebaseAuthService.cerrarSesion()
                                currentScreen = Screen.Login
                            }
                        }
                    )
                }

                // LISTA DE COMPRAS
                is Screen.ShoppingList -> {
                    ShoppingListScreen(
                        authService = authService,
                        listaCompraService = listaCompraService,
                        productoService = productoService,
                        onBack = {
                            // Definir posteriormente
                        },
                        onLogout = {
                            scope.launch {
                                firebaseAuthService.cerrarSesion()
                                currentScreen = Screen.Login
                            }
                        },
                        onNavigateToPromociones = {
                            promosVolverA = Screen.ShoppingList
                            currentScreen = Screen.Promotions
                        }
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

                // PRODUCTOS
                is Screen.Products -> {
                    ProductManagementScreen(
                        productoService = productoService,
                        categoriaService = categoriaService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        }
                    )
                }

                // CATEGORÍAS
                is Screen.Categories -> {
                    CategoryManagementScreen(
                        categoriaService = categoriaService,
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
                        onNavigate = { tab ->
                            when (tab) {
                                ClientTab.INICIO,
                                ClientTab.LISTAS -> {
                                    currentScreen = Screen.ShoppingList
                                }

                                else -> Unit
                            }
                        }
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
