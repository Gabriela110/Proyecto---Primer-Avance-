package ui.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import services.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    listaCompraService: ListaCompraService,
    productoService: ProductoService,
    onBack: () -> Unit
) {
    var presupuestoStr by rememberSaveable { mutableStateOf("") }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    // Estados para diálogos modales de acciones
    var mostrarDialogoPresupuesto by remember { mutableStateOf(false) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }

    // Refrescar estados de la lista
    var elementos by remember { mutableStateOf(listaCompraService.obtenerElementos()) }
    var presupuestoActual by remember { mutableStateOf(0.0) } // O el valor que devuelva tu servicio

    fun actualizarVista() {
        elementos = listaCompraService.obtenerElementos()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Presupuesto y Compras - Súper Selectos") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Regresar") }
                }
            )
        },
        bottomBar = {
            // Barra inferior con totales y saldo disponible
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val total = listaCompraService.calcularTotalLista()
                    val saldo = listaCompraService.calcularSaldoDisponible()

                    Text("Total de la lista: $${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
                    Text("Saldo disponible: $${"%.2f".format(saldo)}", style = MaterialTheme.typography.titleMedium, color = if (saldo < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)

                    mensajeFeedback?.let { msg ->
                        Spacer(Modifier.height(4.dp))
                        Text(msg, color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Botones de acciones principales (Equivalentes al menú 1, 2 y 6 de consola)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mostrarDialogoPresupuesto = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Establecer Presupuesto")
                }
                Button(
                    onClick = { mostrarDialogoAgregar = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Agregar Producto")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Tu Lista de Compras", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // Lista de elementos visuales (Equivalente a la opción 5 de consola)
            if (elementos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("La lista de compras está vacía.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(elementos.entries.toList()) { index, entry ->
                        val producto = entry.key
                        val cantidad = entry.value
                        val subtotal = producto.precio * cantidad

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${index + 1}. ${producto.nombre}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Cantidad: $cantidad | Unitario: $${"%.2f".format(producto.precio)}")
                                    Text("Subtotal: $${"%.2f".format(subtotal)}", style = MaterialTheme.typography.bodyMedium)
                                }

                                // Botones de modificar/eliminar...
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para establecer presupuesto (Opción 1)
        if (mostrarDialogoPresupuesto) {
            var nuevoPresupuesto by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { mostrarDialogoPresupuesto = false },
                title = { Text("Establecer Presupuesto") },
                text = {
                    OutlinedTextField(
                        value = nuevoPresupuesto,
                        onValueChange = { nuevoPresupuesto = it },
                        label = { Text("Monto límite ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            val monto = nuevoPresupuesto.toDoubleOrNull() ?: 0.0
                            listaCompraService.establecerPresupuesto(monto)
                            mensajeFeedback = "Presupuesto actualizado correctamente."
                            esError = false
                            mostrarDialogoPresupuesto = false
                            actualizarVista()
                        } catch (e: Exception) {
                            mensajeFeedback = e.message ?: "Error al establecer presupuesto"
                            esError = true
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoPresupuesto = false }) { Text("Cancelar") }
                }
            )
        }

        // Diálogo para agregar producto del catálogo (Opción 2)
        if (mostrarDialogoAgregar) {
            val productosDisponibles = productoService.obtenerProductos()
            var idProductoInput by remember { mutableStateOf("") }
            var cantidadInput by remember { mutableStateOf("1") }

            AlertDialog(
                onDismissRequest = { mostrarDialogoAgregar = false },
                title = { Text("Agregar Producto") },
                text = {
                    Column {
                        Text("Catálogo disponible:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        for (p in productosDisponibles) {
                            Text("ID: ${p.id} - ${p.nombre} ($${p.precio})", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = idProductoInput,
                            onValueChange = { idProductoInput = it },
                            label = { Text("ID del producto") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cantidadInput,
                            onValueChange = { cantidadInput = it },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            val id = idProductoInput.toIntOrNull() ?: 0
                            val cant = cantidadInput.toIntOrNull() ?: 1
                            listaCompraService.agregarProducto(id, cant)
                            mensajeFeedback = "Producto agregado correctamente."
                            esError = false
                            mostrarDialogoAgregar = false
                            actualizarVista()
                        } catch (e: PresupuestoExcedidoException) {
                            mensajeFeedback = "🚫 ${e.message}"
                            esError = true
                        } catch (e: Exception) {
                            mensajeFeedback = e.message ?: "Error al agregar producto"
                            esError = true
                        }
                    }) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") }
                }
            )
        }
    }
}