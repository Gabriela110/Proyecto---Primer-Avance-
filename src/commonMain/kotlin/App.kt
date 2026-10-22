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
import ui.client.ClientTab
import ui.client.PromocionesScreen
import ui.client.ShoppingListScreen

@Composable
fun App() {
    // Instanciamos los servicios principales
    val authService = remember { AuthService() }
    val productoService = remember { ProductoService() }
    val listaCompraService = remember { ListaCompraService(productoService) }
    val categoriaService = remember { CategoriaService() }
    categoriaService.cargarCategoriasIniciales()
    productoService.cargarProductosIniciales(categoriaService)
    val promocionService = remember { PromocionService() }
    // Se ejecuta una sola vez, después de cargar los productos
    remember { promocionService.cargarPromocionesIniciales(productoService) }
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
                            // currentScreen = Screen.Products (o su respectiva ruta)
                        },
                        onNavigateToPromociones = {
                            promosVolverA = Screen.AdminDashboard
                            currentScreen = Screen.Promotions
                        },
                        onNavigateToCategorias = {
                            // Navegación a categorías cuando esté lista
                        },
                        onNavigateToUsuarios = {
                            // Acción adicional si se requiere
                        },
                        onLogout = {
                            currentScreen = Screen.Login
                        }
                    )
                }

                is Screen.ShoppingList -> {
                    ShoppingListScreen(
                        authService = authService,
                        listaCompraService = listaCompraService, // Tu instancia compartida del servicio
                        productoService = productoService,       // Tu instancia de productos
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

                is Screen.Products -> {
                    // Aquí pondremos la vista de Productos
                }

                is Screen.Promotions -> {
                    PromocionesScreen(
                        promocionService = promocionService,
                        productoService = productoService,
                        onBack = { currentScreen = promosVolverA },
                        // La barra inferior es del cliente; el administrador no la ve
                        mostrarBarraInferior = promosVolverA == Screen.ShoppingList,
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