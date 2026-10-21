import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import services.ListaCompraService
import services.ProductoService
import ui.Screen
import ui.theme.SuperAppTheme
import ui.auth.SuperAppNav
import ui.client.ShoppingListScreen

@Composable
fun App() {
    val productoService = remember { ProductoService() }
    val listaCompraService = remember { ListaCompraService(productoService) }
    MaterialTheme {
        // Mantiene la pantalla activa de TODA la aplicación (inicia en Login)
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

        // Mapeo dinámico: dibuja la pantalla que coincida con currentScreen
        SuperAppTheme {
            when (currentScreen) {
                is Screen.Login, is Screen.Register -> {
                    SuperAppNav(
                        // Este callback se activa cuando el usuario inicia sesión exitosamente
                        onNavigateToMain = {
//                            esAdministrador ->
                            // Aquí validamos el rol devuelto por el servicio de autenticación
//                            if (esAdministrador) {
//                                // TODO: Cambiar a Screen.AdminDashboard cuando esté lista la vista
//                                currentScreen = Screen.AdminDashboard
//                            } else {
                                currentScreen = Screen.ShoppingList
//                            }
                        },
                        // Si el usuario se registra exitosamente, SuperAppNav debe volver a la pantalla de Login
//                        onRegistrationComplete = {
//                            currentScreen = Screen.Login
//                        }
                    )
                }
                is Screen.AdminDashboard -> {
                    // Pantalla temporal de Admin
//                    AdminPlaceholderScreen(
//                        onLogout = { currentScreen = Screen.Login }
//                    )
                }

                is Screen.ShoppingList -> {
                    ShoppingListScreen(
                        listaCompraService = listaCompraService, // Tu instancia compartida del servicio
                        productoService = productoService,       // Tu instancia de productos
                        onBack = {
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