package ui.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import models.Producto
import services.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    authService: AuthService,
    listaCompraService: ListaCompraService,
    productoService: ProductoService,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToPromociones: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }
    var productosDisponibles by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Estados para diálogos modales de acciones
    var mostrarDialogoPresupuesto by remember { mutableStateOf(false) }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var productoModificando by remember { mutableStateOf<Pair<Producto, Int>?>(null) }

    // Estados corregidos con valor inicial explícito y tipado seguro para Compose
    var elementos: Map<Producto, Int> by remember { mutableStateOf(emptyMap()) }
    var saldo by remember { mutableStateOf(0.0) }
    var cargando by remember { mutableStateOf(true) }

    fun actualizarVista() {
        coroutineScope.launch {
            try {
                cargando = true
                elementos = listaCompraService.obtenerElementos()
                saldo = listaCompraService.calcularSaldoDisponible()
                esError = false
            } catch (e: Exception) {
                mensajeFeedback = e.message ?: "Error al actualizar la lista"
                esError = true
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        actualizarVista()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Mi lista de compras", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Column {
                        val total = listaCompraService.calcularTotalLista()
                        Text("Total de la lista: $${"%.2f".format(total)}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Saldo disponible: $${"%.2f".format(saldo)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (saldo < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        mensajeFeedback?.let { msg ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                msg,
                                color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            authService.cerrarSesion()
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cerrar Sesión")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mostrarDialogoPresupuesto = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Presupuesto", maxLines = 1)
                }
                Button(
                    onClick = { mostrarDialogoAgregar = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Producto", maxLines = 1)
                }
            }

            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = onNavigateToPromociones,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver promociones")
            }

            Spacer(Modifier.height(16.dp))
            Text("Tu Lista de Compras", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (elementos.isEmpty()) {
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { productoModificando = Pair(producto, cantidad) }) {
                                        Text("Editar")
                                    }
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    listaCompraService.eliminarProducto(producto.id)
                                                    mensajeFeedback = "Producto eliminado de la lista."
                                                    esError = false
                                                    actualizarVista()
                                                } catch (e: Exception) {
                                                    mensajeFeedback = e.message ?: "Error al eliminar producto"
                                                    esError = true
                                                }
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
        }

        // Diálogo para establecer presupuesto
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
                        val monto = nuevoPresupuesto.toDoubleOrNull()
                        if (monto == null || monto < 0) {
                            mensajeFeedback = "Ingrese un monto numérico válido."
                            esError = true
                            return@Button
                        }
                        coroutineScope.launch {
                            try {
                                listaCompraService.establecerPresupuesto(monto)
                                mensajeFeedback = "Presupuesto actualizado correctamente."
                                esError = false
                                mostrarDialogoPresupuesto = false
                                actualizarVista()
                            } catch (e: Exception) {
                                mensajeFeedback = e.message ?: "Error al establecer presupuesto"
                                esError = true
                            }
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

        // Diálogo para agregar producto del catálogo
        if (mostrarDialogoAgregar) {
            LaunchedEffect(Unit) {
                try {
                    productosDisponibles = productoService.obtenerProductos()
                } catch (e: Exception) {
                    productosDisponibles = emptyList()
                }
            }
            var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
            var cantidadInput by remember { mutableStateOf("1") }

            LaunchedEffect(productosDisponibles) {
                if (productosDisponibles.isNotEmpty() && productoSeleccionado == null) {
                    productoSeleccionado = productosDisponibles.first()
                }
            }

            AlertDialog(
                onDismissRequest = { mostrarDialogoAgregar = false },
                title = { Text("Agregar Producto") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Seleccione un producto del catálogo:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))

                        if (productosDisponibles.isEmpty()) {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(productosDisponibles.size) { index ->
                                    val p = productosDisponibles[index]
                                    val isSelected = productoSeleccionado?.id == p.id

                                    Surface(
                                        selected = isSelected,
                                        onClick = { productoSeleccionado = p },
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(p.nombre, style = MaterialTheme.typography.bodyLarge)
                                            Text("Precio: $${"%.2f".format(p.precio)}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cantidadInput,
                            onValueChange = { cantidadInput = it },
                            label = { Text("Cantidad") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = productoSeleccionado?.id
                            val cant = cantidadInput.toIntOrNull() ?: 0

                            if (id == null) {
                                mensajeFeedback = "Debe seleccionar un producto."
                                esError = true
                                return@Button
                            }
                            if (cant <= 0) {
                                mensajeFeedback = "La cantidad debe ser mayor a 0."
                                esError = true
                                return@Button
                            }

                            coroutineScope.launch {
                                try {
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
                            }
                        },
                        enabled = productosDisponibles.isNotEmpty()
                    ) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoAgregar = false }) { Text("Cancelar") }
                }
            )
        }

        // Diálogo para modificar cantidad
        productoModificando?.let { (prod, cantActual) ->
            var nuevaCantidadInput by remember { mutableStateOf(cantActual.toString()) }

            AlertDialog(
                onDismissRequest = { productoModificando = null },
                title = { Text("Modificar Cantidad") },
                text = {
                    Column {
                        Text("Producto: ${prod.nombre}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nuevaCantidadInput,
                            onValueChange = { nuevaCantidadInput = it },
                            label = { Text("Nueva Cantidad") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val nuevaCant = nuevaCantidadInput.toIntOrNull() ?: 0
                        if (nuevaCant <= 0) {
                            mensajeFeedback = "La cantidad debe ser mayor a 0."
                            esError = true
                            return@Button
                        }

                        coroutineScope.launch {
                            try {
                                listaCompraService.modificarCantidad(prod.id, nuevaCant)
                                mensajeFeedback = "Cantidad actualizada correctamente."
                                esError = false
                                productoModificando = null
                                actualizarVista()
                            } catch (e: PresupuestoExcedidoException) {
                                mensajeFeedback = "🚫 ${e.message}"
                                esError = true
                            } catch (e: Exception) {
                                mensajeFeedback = e.message ?: "Error al modificar la cantidad"
                                esError = true
                            }
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productoModificando = null }) { Text("Cancelar") }
                }
            )
        }
    }
}