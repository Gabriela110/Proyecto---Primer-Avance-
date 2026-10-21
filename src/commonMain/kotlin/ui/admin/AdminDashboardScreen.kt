package ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.AuthService

@Composable
fun AdminDashboardScreen(
    authService: AuthService,
    onNavigateToProductos: () -> Unit,
    onNavigateToPromociones: () -> Unit,
    onNavigateToCategorias: () -> Unit,
    onNavigateToUsuarios: () -> Unit, // Opcional si deseas mostrar los usuarios en pantalla o consola
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SISTEMA DE GESTIÓN ADMINISTRATIVA",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Gestionar productos
        Button(
            onClick = onNavigateToProductos,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Gestionar productos")
        }

        // 2. Gestionar promociones
        Button(
            onClick = onNavigateToPromociones,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Gestionar promociones")
        }

        // 3. Gestionar categorías
        Button(
            onClick = onNavigateToCategorias,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Gestionar categorías")
        }

        // 5. Ver usuarios registrados (Llama directamente al método original del AuthService)
        Button(
            onClick = {
                authService.listarUsuarios()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text("Ver usuarios registrados (Consola)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Salir / Cerrar sesión
        OutlinedButton(
            onClick = {
                authService.cerrarSesion()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar sesión")
        }
    }
}