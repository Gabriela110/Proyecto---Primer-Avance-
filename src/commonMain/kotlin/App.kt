import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
//import services.AuthService
import services.CategoriaService
import services.FirebaseAuthService
import services.ListaCompraService
import services.ProductoService
import services.PromocionService
import ui.Screen
import ui.admin.AdminDashboardScreen
import ui.admin.CategoryManagementScreen
import ui.admin.ProductManagementScreen
import ui.admin.UsuariosScreen
import ui.auth.LoginScreen
import ui.auth.RegisterScreen
import ui.client.ClientTab
import ui.client.PromocionesScreen
import ui.client.ShoppingListScreen
import ui.theme.SuperAppTheme

@Composable
fun App() {
    // Instanciamos los servicios principales
    //val authService = remember { AuthService() }
    val firebaseAuthService = remember { FirebaseAuthService() }

    val productoService = remember { ProductoService() }
    val promocionService = remember { PromocionService() }
    val listaCompraService = remember { ListaCompraService(productoService, promocionService) }
    val categoriaService = remember { CategoriaService() }

    val scope = rememberCoroutineScope()

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
                    firebaseAuthService = firebaseAuthService,
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
                    firebaseAuthService = firebaseAuthService,
                    listaCompraService = listaCompraService,
                    productoService = productoService,
                    onNavigate = { tab ->
                        when (tab) {
                            ClientTab.PROMOS -> {
                                promosVolverA = Screen.ShoppingList
                                currentScreen = Screen.Promotions
                            }
                            ClientTab.INICIO,
                            ClientTab.LISTAS -> {
                                currentScreen = Screen.ShoppingList
                            }
                            else -> Unit
                        }
                    },
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
                    mostrarBarraInferior = promosVolverA == Screen.ShoppingList,
                    modoAdmin = promosVolverA == Screen.AdminDashboard,
                    onNavigate = { tab ->
                        when (tab) {
                            ClientTab.INICIO,
                            ClientTab.LISTAS -> {
                                currentScreen = Screen.ShoppingList
                            }
                            ClientTab.PROMOS -> {
                                currentScreen = Screen.Promotions
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