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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.Producto
import services.ListaCompraService
import services.ProductoService
import ui.components.CampoBusqueda
import ui.components.DialogoCantidad
import ui.components.EncabezadoVerde
import ui.components.dinero
import ui.components.formatoDescuento

/**
 * Catálogo de productos para el cliente (pestaña "Productos").
 * Permite buscar, filtrar por categoría y agregar productos a la lista de compras.
 * Muestra el precio con la promoción vigente, la misma que se cobra en la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    productoService: ProductoService,
    listaCompraService: ListaCompraService,
    onNavigate: (ClientTab) -> Unit
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }
    var productoAAgregar by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    val productos = productoService.obtenerProductos()
    val categorias = productos.map { it.categoria.nombre }.distinct()

    val texto = busqueda.trim()
    val filtrados = productos.filter {
        val coincideTexto = texto.isEmpty() ||
            it.nombre.contains(texto, ignoreCase = true) ||
            it.categoria.nombre.contains(texto, ignoreCase = true)
        val coincideCategoria = categoriaSeleccionada == null || it.categoria.nombre == categoriaSeleccionada
        coincideTexto && coincideCategoria
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { EncabezadoVerde(titulo = "Productos") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { ClientBottomBar(seleccionada = ClientTab.PRODUCTOS, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CampoBusqueda(
                valor = busqueda,
                onCambio = { busqueda = it },
                placeholder = "Buscar productos...",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = categoriaSeleccionada == null,
                        onClick = { categoriaSeleccionada = null },
                        label = { Text("Todas") }
                    )
                }
                items(categorias) { nombre ->
                    FilterChip(
                        selected = categoriaSeleccionada == nombre,
                        onClick = { categoriaSeleccionada = nombre },
                        label = { Text(nombre) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (productos.isEmpty()) "Todavía no hay productos disponibles."
                        else "No encontramos productos con ese filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { producto ->
                        TarjetaCatalogo(
                            producto = producto,
                            precioUnitario = listaCompraService.precioUnitario(producto),
                            descuento = listaCompraService.descuentoDe(producto),
                            onAgregar = { productoAAgregar = producto }
                        )
                    }
                }
            }
        }
    }

    // ---- Agregar a la lista ----
    productoAAgregar?.let { producto ->
        DialogoCantidad(
            titulo = "Agregar a mi lista",
            detalle = "${producto.nombre} · \$${dinero(listaCompraService.precioUnitario(producto))} c/u",
            cantidadInicial = 1,
            textoConfirmar = "Agregar",
            onConfirmar = { cantidad ->
                try {
                    listaCompraService.agregarProducto(producto.id, cantidad)
                    mensaje = "«${producto.nombre}» agregado a tu lista."
                    productoAAgregar = null
                    null
                } catch (e: Exception) {
                    e.message ?: "No se pudo agregar el producto"
                }
            },
            onCancelar = { productoAAgregar = null }
        )
    }
}

@Composable
private fun TarjetaCatalogo(
    producto: Producto,
    precioUnitario: Double,
    descuento: Double,
    onAgregar: () -> Unit
) {
    val agotado = producto.stock <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = producto.categoria.nombre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\$${dinero(precioUnitario)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (descuento > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "\$${dinero(producto.precio)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "-${formatoDescuento(descuento)}%",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (agotado) "Agotado" else "Disponibles: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (agotado) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(12.dp))

            Button(onClick = onAgregar, enabled = !agotado) {
                Text("Agregar")
            }
        }
    }
}
