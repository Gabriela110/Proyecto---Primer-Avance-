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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.Producto
import services.ListaCompraService
import ui.components.DialogoCantidad
import ui.components.EncabezadoVerde
import ui.components.dinero
import ui.components.formatoDescuento
import ui.components.parseDecimal

/**
 * Lista de compras del cliente.
 *
 * - Resumen del presupuesto (total, saldo y barra de avance).
 * - Cada producto se puede editar (cantidad) o quitar de la lista.
 * - Los productos se agregan desde la pestaña "Productos".
 * - Los precios ya incluyen las promociones vigentes.
 */
@Composable
fun ShoppingListScreen(
    listaCompraService: ListaCompraService,
    onNavigate: (ClientTab) -> Unit
) {
    // Los servicios guardan listas simples (no observables): este contador refresca la pantalla
    var version by remember { mutableStateOf(0) }

    var mostrarPresupuesto by remember { mutableStateOf(false) }
    var productoEnEdicion by remember { mutableStateOf<Producto?>(null) }
    var productoAQuitar by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    val elementos = version.let { listaCompraService.obtenerElementos().entries.toList() }
    val total = listaCompraService.calcularTotalLista()
    val saldo = listaCompraService.calcularSaldoDisponible()
    val presupuesto = listaCompraService.obtenerPresupuesto()
    val hayPresupuesto = listaCompraService.hayPresupuesto()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { EncabezadoVerde(titulo = "Mi lista de compras") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { ClientBottomBar(seleccionada = ClientTab.LISTAS, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ResumenPresupuesto(
                hayPresupuesto = hayPresupuesto,
                presupuesto = presupuesto,
                total = total,
                saldo = saldo,
                onCambiarPresupuesto = { mostrarPresupuesto = true },
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Tus productos (${elementos.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))

            if (elementos.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
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
                    items(elementos, key = { it.key.id }) { entry ->
                        TarjetaElemento(
                            producto = entry.key,
                            cantidad = entry.value,
                            precioUnitario = listaCompraService.precioUnitario(entry.key),
                            descuento = listaCompraService.descuentoDe(entry.key),
                            onEditar = { productoEnEdicion = entry.key },
                            onQuitar = { productoAQuitar = entry.key }
                        )
                    }
                }
            }
        }
    }

    // ---- Establecer presupuesto ----
    if (mostrarPresupuesto) {
        DialogoPresupuesto(
            presupuestoActual = if (hayPresupuesto) presupuesto else null,
            onGuardar = { monto ->
                try {
                    listaCompraService.establecerPresupuesto(monto)
                    mensaje = "Presupuesto actualizado correctamente."
                    version += 1
                    mostrarPresupuesto = false
                    null
                } catch (e: Exception) {
                    e.message ?: "No se pudo establecer el presupuesto"
                }
            },
            onCancelar = { mostrarPresupuesto = false }
        )
    }

    // ---- Cambiar cantidad ----
    productoEnEdicion?.let { producto ->
        DialogoCantidad(
            titulo = "Cambiar cantidad",
            detalle = "${producto.nombre} · \$${dinero(listaCompraService.precioUnitario(producto))} c/u",
            cantidadInicial = listaCompraService.obtenerElementos()[producto] ?: 1,
            textoConfirmar = "Guardar",
            onConfirmar = { cantidad ->
                try {
                    listaCompraService.modificarCantidad(producto.id, cantidad)
                    mensaje = "Cantidad actualizada."
                    version += 1
                    productoEnEdicion = null
                    null
                } catch (e: Exception) {
                    e.message ?: "No se pudo actualizar la cantidad"
                }
            },
            onCancelar = { productoEnEdicion = null }
        )
    }

    // ---- Quitar de la lista (con confirmación) ----
    productoAQuitar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAQuitar = null },
            title = { Text("Quitar producto") },
            text = { Text("¿Querés quitar \"${producto.nombre}\" de tu lista?") },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            listaCompraService.eliminarProducto(producto.id)
                            mensaje = "Producto quitado de la lista."
                        } catch (e: Exception) {
                            mensaje = e.message ?: "No se pudo quitar el producto"
                        }
                        version += 1
                        productoAQuitar = null
                    }
                ) { Text("Quitar") }
            },
            dismissButton = {
                TextButton(onClick = { productoAQuitar = null }) { Text("Cancelar") }
            }
        )
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
                        text = if (hayPresupuesto) "\$${dinero(presupuesto)}" else "Sin definir",
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
                Text("Total: \$${dinero(total)}", style = MaterialTheme.typography.bodyMedium)
                if (hayPresupuesto) {
                    Text(
                        text = if (excedido) "Excedido: \$${dinero(-saldo)}" else "Disponible: \$${dinero(saldo)}",
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
                        text = "$cantidad × \$${dinero(precioUnitario)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (descuento > 0) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "\$${dinero(producto.precio)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "-${formatoDescuento(descuento)}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "Subtotal: \$${dinero(precioUnitario * cantidad)}",
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
    var texto by remember { mutableStateOf(presupuestoActual?.let { dinero(it) } ?: "") }
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
                val monto = parseDecimal(texto)
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
