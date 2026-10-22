package ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Producto
import models.Promocion
import services.ProductoService
import services.PromocionService
import kotlin.math.roundToInt

/**
 * Pantalla de Promociones (mockup de la Etapa 1).
 *
 * - Encabezado verde con flecha de regreso y título.
 * - Buscador (por nombre de promoción, producto o categoría) y filtro por categoría.
 * - Tarjetas de colores con el descuento, el producto y el precio antes/ahora.
 * - Barra de navegación inferior con "Promos" seleccionada.
 *
 * Los datos salen de PromocionService y ProductoService (los mismos de la Etapa 2).
 */
@Composable
fun PromocionesScreen(
    promocionService: PromocionService,
    productoService: ProductoService,
    onBack: () -> Unit,
    onNavigate: (ClientTab) -> Unit = {},
    mostrarBarraInferior: Boolean = true
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }

    // Se lee en cada recomposición para reflejar altas/bajas hechas desde el administrador
    val todas = promocionService.obtenerPromociones().map { promo ->
        PromoItem(promo, productoService.buscarPorId(promo.productoId))
    }
    val categorias = todas.mapNotNull { it.producto?.categoria?.nombre }.distinct()

    val filtradas = todas.filter { item ->
        val texto = busqueda.trim()
        val coincideTexto = texto.isEmpty() ||
            item.promocion.nombre.contains(texto, ignoreCase = true) ||
            item.producto?.nombre?.contains(texto, ignoreCase = true) == true ||
            item.producto?.categoria?.nombre?.contains(texto, ignoreCase = true) == true
        val coincideCategoria = categoriaSeleccionada == null ||
            item.producto?.categoria?.nombre == categoriaSeleccionada
        coincideTexto && coincideCategoria
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { EncabezadoPromociones(onBack) },
        bottomBar = {
            if (mostrarBarraInferior) {
                ClientBottomBar(seleccionada = ClientTab.PROMOS, onNavigate = onNavigate)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BarraBusqueda(
                busqueda = busqueda,
                onBusquedaChange = { busqueda = it },
                categorias = categorias,
                categoriaSeleccionada = categoriaSeleccionada,
                onCategoriaChange = { categoriaSeleccionada = it }
            )

            if (filtradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (todas.isEmpty()) {
                            "Aún no hay promociones disponibles."
                        } else {
                            "No encontramos promociones con ese filtro."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filtradas, key = { _, item -> item.promocion.id }) { index, item ->
                        TarjetaPromocion(item, PALETAS[index % PALETAS.size])
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Partes de la pantalla
// ---------------------------------------------------------------------------------------------

@Composable
private fun EncabezadoPromociones(onBack: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Promociones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun BarraBusqueda(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    categorias: List<String>,
    categoriaSeleccionada: String?,
    onCategoriaChange: (String?) -> Unit
) {
    var menuAbierto by rememberSaveable { mutableStateOf(false) }
    val hayFiltro = categoriaSeleccionada != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = busqueda,
            onValueChange = onBusquedaChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Buscar promociones...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = { onBusquedaChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                    }
                }
            }
        )

        Spacer(Modifier.width(8.dp))

        Box {
            IconButton(
                onClick = { menuAbierto = true },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (hayFiltro) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = "Filtrar por categoría",
                    tint = if (hayFiltro) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                DropdownMenuItem(
                    text = { Text("Todas las categorías") },
                    onClick = {
                        onCategoriaChange(null)
                        menuAbierto = false
                    }
                )
                categorias.forEach { nombre ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                nombre,
                                fontWeight = if (nombre == categoriaSeleccionada) FontWeight.Bold
                                else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onCategoriaChange(nombre)
                            menuAbierto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPromocion(item: PromoItem, colores: PaletaPromo) {
    val promo = item.promocion
    val producto = item.producto

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colores.fondo,
        contentColor = colores.texto,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${formatoDescuento(promo.descuento)}% de descuento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = promo.nombre, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(10.dp))

                if (producto != null) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val precioFinal = producto.precio * (1 - promo.descuento / 100.0)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\$${dinero(producto.precio)}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = colores.texto.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "\$${dinero(precioFinal)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Producto no disponible",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // No hay fotos de productos en el proyecto: se usa un ícono según la categoría
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(colores.texto.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoPorCategoria(producto?.categoria?.nombre),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------------------------

private data class PromoItem(val promocion: Promocion, val producto: Producto?)

private class PaletaPromo(val fondo: Color, val texto: Color)

/** Morada, azul y naranja, como en el mockup. Se alternan por posición en la lista. */
private val PALETAS = listOf(
    PaletaPromo(fondo = Color(0xFF5B2C6F), texto = Color.White),
    PaletaPromo(fondo = Color(0xFFDCE6F7), texto = Color(0xFF1B2A49)),
    PaletaPromo(fondo = Color(0xFFFCE8C8), texto = Color(0xFF4A3410))
)

private fun iconoPorCategoria(categoria: String?): ImageVector = when {
    categoria == null -> Icons.Filled.LocalOffer
    categoria.contains("limpieza", ignoreCase = true) -> Icons.Filled.CleaningServices
    categoria.contains("bebida", ignoreCase = true) -> Icons.Filled.LocalDrink
    categoria.contains("aliment", ignoreCase = true) -> Icons.Filled.Restaurant
    categoria.contains("higiene", ignoreCase = true) -> Icons.Filled.Spa
    else -> Icons.Filled.LocalOffer
}

/** 20.0 -> "20", 12.5 -> "12.5" */
private fun formatoDescuento(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toInt().toString() else valor.toString()

/** Formatea con 2 decimales sin depender de String.format (que no es multiplataforma). */
private fun dinero(valor: Double): String {
    val centavos = (valor * 100).roundToInt()
    return "${centavos / 100}.${(centavos % 100).toString().padStart(2, '0')}"
}
