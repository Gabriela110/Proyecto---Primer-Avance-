package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF2E7D32)       // Verde principal / institucional
val PrimaryGreenDark = Color(0xFF1B5E20)   // Verde oscuro para contrastes o modo oscuro
val SecondaryGreen = Color(0xFF4CAF50)     // Verde secundario / acentos
val BackgroundLight = Color(0xFFF8F9FA)    // Fondo claro limpio
val SurfaceLight = Color(0xFFFFFFFF)       // Superficies blancas (Cards, TopBar)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF002204),
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C19)
)

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryGreen,
    onPrimary = Color(0xFF00390B),
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = PrimaryGreen,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun SuperAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}