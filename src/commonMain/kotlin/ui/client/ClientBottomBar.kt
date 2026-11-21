package ui.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

/** Pestañas de la barra inferior del cliente (según el mockup de la Etapa 1). */
enum class ClientTab(val etiqueta: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Filled.Home),
    LISTAS("Listas", Icons.Filled.ShoppingCart),
    PRODUCTOS("Productos", Icons.Filled.Inventory2),
    PROMOS("Promos", Icons.Filled.LocalOffer),
    MAS("Más", Icons.Filled.Apps)
}

@Composable
fun ClientBottomBar(
    seleccionada: ClientTab,
    onNavigate: (ClientTab) -> Unit
) {
    NavigationBar {
        ClientTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == seleccionada,
                onClick = { onNavigate(tab) },
                icon = { Icon(tab.icono, contentDescription = tab.etiqueta) },
                label = { Text(tab.etiqueta, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
