import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

fun main() = application {
    Firebase.initialize()
    Window(
        onCloseRequest = ::exitApplication,
        title = "SuperApp - Escritorio"
    ) {
        App()
    }
}