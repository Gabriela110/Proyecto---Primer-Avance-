package ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import services.CategoriaService
import services.ProductoService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    productoService: ProductoService,
    categoriaService: CategoriaService,
    onBack: () -> Unit
) {
    // El estado toma la lista inicial gracias al .toList() del servicio
    var productos by remember { mutableStateOf(productoService.obtenerProductos()) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    // Función que refresca el estado con la nueva referencia de la lista
    fun actualizarLista() {
        productos = productoService.obtenerProductos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos - Admin") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Volver")
                    }
                }
            )
        },
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
            Text("Catálogo de Productos", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            mensajeFeedback?.let { msg ->
                Text(
                    text = msg,
                    color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No existen productos actualmente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productos) { producto ->
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
                                    Text("ID: ${producto.id} - ${producto.nombre}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Precio: $${producto.precio} | Stock: ${producto.stock}")
                                    Text("Categoría: ${producto.categoria.nombre}", style = MaterialTheme.typography.bodySmall)
                                }

                                // Botón para eliminar
                                TextButton(
                                    onClick = {
                                        val eliminado = productoService.eliminarProducto(producto)
                                        if (eliminado) {
                                            mensajeFeedback = "Producto eliminado correctamente."
                                            esError = false
                                            actualizarLista()
                                        } else {
                                            mensajeFeedback = "Error al eliminar el producto."
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

        // Diálogo para Crear Producto
        if (mostrarDialogoCrear) {
            val categorias = categoriaService.obtenerCategorias()
            var nombreInput by remember { mutableStateOf("") }
            var precioInput by remember { mutableStateOf("") }
            var stockInput by remember { mutableStateOf("") }
            var categoriaSeleccionada by remember { mutableStateOf(categorias.firstOrNull()) }

            AlertDialog(
                onDismissRequest = { mostrarDialogoCrear = false },
                title = { Text("Crear Nuevo Producto") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (categorias.isEmpty()) {
                            Text("No existen categorías. Cree una categoría primero.", color = MaterialTheme.colorScheme.error)
                        } else {
                            OutlinedTextField(
                                value = nombreInput,
                                onValueChange = { nombreInput = it },
                                label = { Text("Nombre del producto") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = precioInput,
                                onValueChange = { precioInput = it },
                                label = { Text("Precio") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = stockInput,
                                onValueChange = { stockInput = it },
                                label = { Text("Stock") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(4.dp))
                            Text("Seleccione una categoría:", style = MaterialTheme.typography.bodySmall)

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(categorias) { cat ->
                                    val isSelected = categoriaSeleccionada?.id == cat.id
                                    Surface(
                                        selected = isSelected,
                                        onClick = { categoriaSeleccionada = cat },
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "ID: ${cat.id} | ${cat.nombre}",
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val precio = precioInput.toDoubleOrNull() ?: throw Exception("Precio inválido")
                                val stock = stockInput.toIntOrNull() ?: throw Exception("Stock inválido")
                                val cat = categoriaSeleccionada ?: throw Exception("Debe seleccionar una categoría")

                                productoService.agregarProducto(nombreInput, precio, stock, cat)
                                mensajeFeedback = "Producto creado correctamente."
                                esError = false
                                mostrarDialogoCrear = false
                                actualizarLista()
                            } catch (e: Exception) {
                                mensajeFeedback = e.message ?: "Error al crear el producto"
                                esError = true
                            }
                        },
                        enabled = categorias.isNotEmpty()
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