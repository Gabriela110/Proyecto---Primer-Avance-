package ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import services.CategoriaService
import ui.components.EncabezadoVerde

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    categoriaService: CategoriaService,
    onBack: () -> Unit
) {
    var categorias by remember { mutableStateOf(categoriaService.obtenerCategorias()) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    fun actualizarLista() {
        categorias = categoriaService.obtenerCategorias()
    }

    Scaffold(
        topBar = { EncabezadoVerde(titulo = "Gestionar Categorías", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoCrear = true }) {
                Text("+ Crear")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Catálogo de Categorías", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            mensajeFeedback?.let { msg ->
                Text(
                    text = msg,
                    color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            if (categorias.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No existen categorías actualmente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categorias) { categoria ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ID: ${categoria.id}", style = MaterialTheme.typography.bodySmall)
                                    Text("Nombre: ${categoria.nombre}", style = MaterialTheme.typography.bodyLarge)
                                }

                                TextButton(
                                    onClick = {
                                        val eliminado = categoriaService.eliminarCategoria(categoria)
                                        if (eliminado) {
                                            mensajeFeedback = "Categoría eliminada correctamente."
                                            esError = false
                                            actualizarLista()
                                        } else {
                                            mensajeFeedback = "Error al eliminar la categoría."
                                            esError = true
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para Crear Categoría
        if (mostrarDialogoCrear) {
            var nombreInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { mostrarDialogoCrear = false },
                title = { Text("Crear Nueva Categoría") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nombreInput,
                            onValueChange = { nombreInput = it },
                            label = { Text("Nombre de la categoría") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                if (nombreInput.isBlank()) throw Exception("El nombre no puede estar vacío")
                                categoriaService.agregarCategoria(nombreInput.trim())
                                mensajeFeedback = "Categoría creada correctamente."
                                esError = false
                                mostrarDialogoCrear = false
                                actualizarLista()
                            } catch (e: Exception) {
                                mensajeFeedback = e.message ?: "Error al crear la categoría"
                                esError = true
                            }
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoCrear = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}