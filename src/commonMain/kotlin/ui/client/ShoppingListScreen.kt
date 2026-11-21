package ui.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import models.Producto
import services.FirebaseAuthService
import services.ListaCompraService
import services.PresupuestoExcedidoException
import services.ProductoService
import utils.Utils

/*
 * Lista de compras del cliente.
 *
 * - Resumen del presupuesto (total, saldo y barra de avance).
 * - Cada producto se puede editar (cantidad) o quitar de la lista.
 * - Los productos se agregan desde la pestaña "Productos" o desde el diálogo.
 * - Los precios ya incluyen las promociones vigentes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    firebaseAuthService: FirebaseAuthService,
    listaCompraService: ListaCompraService,
    productoService: ProductoService,
    onNavigate: (ClientTab) -> Unit,
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

    // Estados reactivos sincronizados con el servicio
    var elementos: Map<Producto, Int> by remember { mutableStateOf(emptyMap()) }
    var total by remember { mutableStateOf(0.0) }
    var saldo by remember { mutableStateOf(0.0) }
    var presupuesto by remember { mutableStateOf(0.0) }
    var cargando by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val hayPresupuesto = presupuesto > 0.0 || listaCompraService.hayPresupuesto()

    fun actualizarVista() {
        coroutineScope.launch {
            try {
                cargando = true
                productoService.obtenerProductos()
                elementos = listaCompraService.obtenerElementos()
                presupuesto = listaCompraService.obtenerPresupuesto()
                total = elementos.entries.sumOf { (prod, cant) ->
                    listaCompraService.precioUnitario(prod) * cant
                }
                saldo = presupuesto - total
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

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Column {
                        Text(
                            "Total de la lista: $${"%.2f".format(total)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                            coroutineScope.launch {
                                firebaseAuthService.cerrarSesion()
                                onLogout()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

            ResumenPresupuesto(
                hayPresupuesto = hayPresupuesto,
                presupuesto = presupuesto,
                total = total,
                saldo = saldo,
                onCambiarPresupuesto = { mostrarDialogoPresupuesto = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = "Tus productos (${elementos.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (elementos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tu lista está vacía.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Agregá productos desde el catálogo.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { onNavigate(ClientTab.PRODUCTOS) }) {
                        Text("Ver productos")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(elementos.entries.toList(), key = { it.key.id }) { entry ->
                        val producto = entry.key
                        val cantidad = entry.value

                        TarjetaElemento(
                            producto = producto,
                            cantidad = cantidad,
                            precioUnitario = listaCompraService.precioUnitario(producto),
                            descuento = listaCompraService.descuentoDe(producto),
                            onEditar = { productoModificando = Pair(producto, cantidad) },
                            onQuitar = {
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
                            }
                        )
                    }
                }
            }
        }

        // Diálogo para establecer presupuesto
        if (mostrarDialogoPresupuesto) {
            DialogoPresupuesto(
                presupuestoActual = if (hayPresupuesto) presupuesto else null,
                onGuardar = { monto ->
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
                    null
                },
                onCancelar = { mostrarDialogoPresupuesto = false }
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
                                items(productosDisponibles, key = { it.id }) { p ->
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

@Composable
private fun ResumenPresupuesto(
    hayPresupuesto: Boolean,
    presupuesto: Double,
    total: Double,
    saldo: Double,
    onCambiarPresupuesto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val excedido = hayPresupuesto && saldo < 0
    val fraccion = if (hayPresupuesto && presupuesto > 0) (total / presupuesto).toFloat().coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Presupuesto", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = if (hayPresupuesto) "\$${Utils.dinero(presupuesto)}" else "Sin definir",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(onClick = onCambiarPresupuesto) {
                    Text(if (hayPresupuesto) "Cambiar" else "Establecer")
                }
            }

            if (hayPresupuesto) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { fraccion },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (excedido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: \$${Utils.dinero(total)}", style = MaterialTheme.typography.bodyMedium)
                if (hayPresupuesto) {
                    Text(
                        text = if (excedido) "Excedido: \$${Utils.dinero(-saldo)}" else "Disponible: \$${Utils.dinero(saldo)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (excedido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaElemento(
    producto: Producto,
    cantidad: Int,
    precioUnitario: Double,
    descuento: Double,
    onEditar: () -> Unit,
    onQuitar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$cantidad × \$${Utils.dinero(precioUnitario)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (descuento > 0) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "\$${Utils.dinero(producto.precio)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "-${Utils.formatoDescuento(descuento)}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "Subtotal: \$${Utils.dinero(precioUnitario * cantidad)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Cambiar cantidad")
            }
            IconButton(onClick = onQuitar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Quitar de la lista",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Devuelve un mensaje de error si el monto no es válido, o null si se guardó bien. */
@Composable
private fun DialogoPresupuesto(
    presupuestoActual: Double?,
    onGuardar: (Double) -> String?,
    onCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(presupuestoActual?.let { Utils.dinero(it) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Establecer presupuesto") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = {
                    texto = it
                    error = null
                },
                label = { Text("Monto límite (\$)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val monto = Utils.parseDecimal(texto)
                error = when {
                    texto.isBlank() -> "El monto es obligatorio"
                    monto == null -> "Ingresá un número válido"
                    monto <= 0 -> "Debe ser mayor a 0"
                    else -> onGuardar(monto)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}