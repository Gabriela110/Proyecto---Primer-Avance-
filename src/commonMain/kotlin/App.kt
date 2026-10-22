import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import services.AuthService
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
    val productoService = remember { ProductoService() }
    val listaCompraService = remember { ListaCompraService(productoService) }
    val categoriaService = remember { CategoriaService() }
    val promocionService = remember { PromocionService() }
    categoriaService.cargarCategoriasIniciales()
    productoService.cargarProductosIniciales(categoriaService)
    promocionService.cargarPromocionesIniciales(productoService)
    MaterialTheme {
        // Mantiene la pantalla activa de TODA la aplicación (inicia en Login)
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
        // Pantalla a la que regresa "Promociones" (lista del cliente o panel del administrador)
        var promosVolverA by remember { mutableStateOf<Screen>(Screen.ShoppingList) }

        // Mapeo dinámico: dibuja la pantalla que coincida con currentScreen
        SuperAppTheme {
            when (currentScreen) {
                is Screen.Login -> {
                    LoginScreen(
                        authService = authService,
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

                is Screen.Register -> {
                    RegisterScreen(
                        authService = authService,
                        onRegisterSuccess = {
                            currentScreen = Screen.Login
                        },
                        onBackToLogin = {
                            currentScreen = Screen.Login
                        }
                    )
                }

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
                            currentScreen = Screen.Login
                        }
                    )
                }

                is Screen.ShoppingList -> {
                    ShoppingListScreen(
                        authService = authService,
                        listaCompraService = listaCompraService,
                        productoService = productoService,
                        onBack = {
                            //Definir posteriormente
                        },
                        onLogout = {
                            currentScreen = Screen.Login
                        },
                        onNavigateToPromociones = {
                            promosVolverA = Screen.ShoppingList
                            currentScreen = Screen.Promotions
                        }
                    )
                }

                is Screen.Users -> {
                    UsuariosScreen(
                        authService = authService,
                        onBack = { currentScreen = Screen.AdminDashboard }
                    )
                }

                is Screen.Products -> {
                    ProductManagementScreen(
                        productoService = productoService,
                        categoriaService = categoriaService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        }
                    )
                }

                is Screen.Categories -> {
                    CategoryManagementScreen(
                        categoriaService = categoriaService,
                        onBack = {
                            currentScreen = Screen.AdminDashboard
                        }
                    )
                }

                is Screen.Promotions -> {
                    PromocionesScreen(
                        promocionService = promocionService,
                        productoService = productoService,
                        onBack = { currentScreen = promosVolverA },
                        // La barra inferior es del cliente; el administrador no la ve
                        mostrarBarraInferior = promosVolverA == Screen.ShoppingList,
                        modoAdmin = promosVolverA == Screen.AdminDashboard,
                        onNavigate = { tab ->
                            when (tab) {
                                ClientTab.INICIO, ClientTab.LISTAS -> currentScreen = Screen.ShoppingList
                                else -> Unit // Productos y Más: pendientes de sus pantallas
                            }
                        }
                    )
                }

                is Screen.Statistics -> {
                    // Aquí pondremos la vista de Estadísticas
                }

                else -> {
                    // Resto de pantallas
                }
            }
        }
    }
}