import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import services.AuthService
import services.CategoriaService
import services.ListaCompraService
import services.ProductoService
import ui.Screen
import ui.theme.SuperAppTheme
import ui.auth.LoginScreen
import ui.auth.RegisterScreen
import ui.admin.AdminDashboardScreen
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
    MaterialTheme {
        // Mantiene la pantalla activa de TODA la aplicación (inicia en Login)
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

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
                            // currentScreen = Screen.Promotions (o su respectiva ruta)
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
                        }
                    )
                }

                is Screen.Products -> {
                    // Aquí pondremos la vista de Productos
                }

                is Screen.Promotions -> {
                    // Aquí pondremos la vista de Promociones
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