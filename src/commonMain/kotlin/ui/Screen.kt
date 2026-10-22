package ui

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object AdminDashboard : Screen()
    object ShoppingList : Screen()
    object Products : Screen()
    object Categories: Screen()
    object Promotions : Screen()
    object Statistics : Screen()
}