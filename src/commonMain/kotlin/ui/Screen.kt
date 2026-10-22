package ui

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object AdminDashboard : Screen()         // Vista Administrador
    object ShoppingList : Screen()
    object Products : Screen()
    object Promotions : Screen()
    object Statistics : Screen()
    object Users : Screen()                  // Vista de usuarios (solo lectura)
}