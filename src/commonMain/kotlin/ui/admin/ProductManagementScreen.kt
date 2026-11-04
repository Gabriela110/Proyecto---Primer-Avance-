package ui.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.Categoria
import models.Producto
import services.CategoriaService
import services.ProductoService
import ui.components.CampoBusqueda
import ui.components.EncabezadoVerde
import ui.components.dinero
import ui.components.parseDecimal

/** Con este stock o menos, el producto se marca como "stock bajo". */
private const val STOCK_BAJO = 5

/**
 * Gestión de productos (administrador): ver, buscar, crear, editar y eliminar.
 *
 * @param onProductoModificado se llama después de editar (la lista de compras reconstruye su índice).
 * @param onProductoEliminado se llama después de eliminar (para borrar sus promociones y quitarlo de la lista).
 */
@Composable
fun ProductManagementScreen(
    productoService: ProductoService,
    categoriaService: CategoriaService,
    onBack: () -> Unit,
    onProductoModificado: () -> Unit = {},
    onProductoEliminado: (Producto) -> Unit = {}
) {
    var busqueda by rememberSaveable { mutableStateOf("") }

    // Los servicios guardan listas simples (no observables): este contador refresca la pantalla
    var version by remember { mutableStateOf(0) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoEnEdicion by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    val productos = version.let { productoService.obtenerProductos() }
    val categorias = categoriaService.obtenerCategorias()

    val texto = busqueda.trim()
    val filtrados = productos.filter {
        texto.isEmpty() ||
            it.nombre.contains(texto, ignoreCase = true) ||
            it.categoria.nombre.contains(texto, ignoreCase = true)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { EncabezadoVerde(titulo = "Gestionar productos", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    productoEnEdicion = null
                    mostrarFormulario = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo producto") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CampoBusqueda(
                valor = busqueda,
                onCambio = { busqueda = it },
                placeholder = "Buscar por nombre o categoría...",
                modifier = Modifier.padding(16.dp)
            )

            if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            productos.isEmpty() ->
                                "Aún no hay productos. Tocá «Nuevo producto» para crear el primero."
                            else -> "No encontramos productos con esa búsqueda."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    // Espacio extra abajo para que el botón flotante no tape la última tarjeta
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtrados, key = { it.id }) { producto ->
                        TarjetaProducto(
                            producto = producto,
                            onEditar = {
                                productoEnEdicion = producto
                                mostrarFormulario = true
                            },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    // ---- Crear / editar ----
    if (mostrarFormulario) {
        FormularioProducto(
            inicial = productoEnEdicion,
            categorias = categorias,
            nombreRepetido = { nombre -> productoService.existeNombre(nombre, productoEnEdicion?.id) },
            onGuardar = { nombre, precio, stock, categoria ->
                val editando = productoEnEdicion
                if (editando == null) {
                    productoService.agregarProducto(nombre, precio, stock, categoria)
                    mensaje = "Producto creado correctamente."
                } else {
                    productoService.actualizarProducto(editando.id, nombre, precio, stock, categoria)
                    onProductoModificado()
                    mensaje = "Producto actualizado correctamente."
                }
                version += 1
                mostrarFormulario = false
                productoEnEdicion = null
            },
            onCancelar = {
                mostrarFormulario = false
                productoEnEdicion = null
            }
        )
    }

    // ---- Eliminar (con confirmación) ----
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = {
                Text(
                    "¿Seguro que querés eliminar \"${producto.nombre}\"? " +
                        "También se quitarán sus promociones y se sacará de las listas de compras. " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (productoService.eliminarProducto(producto)) {
                            onProductoEliminado(producto)
                            mensaje = "Producto eliminado."
                            version += 1
                        } else {
                            mensaje = "No se pudo eliminar el producto."
                        }
                        productoAEliminar = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaProducto(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
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
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = producto.categoria.nombre,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\$${dinero(producto.precio)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (producto.stock == 0) "Agotado" else "Stock: ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (producto.stock <= STOCK_BAJO) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar producto")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar producto",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Formulario para crear (inicial = null) o editar un producto. */
@Composable
private fun FormularioProducto(
    inicial: Producto?,
    categorias: List<Categoria>,
    nombreRepetido: (String) -> Boolean,
    onGuardar: (nombre: String, precio: Double, stock: Int, categoria: Categoria) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var precioTexto by remember { mutableStateOf(inicial?.let { dinero(it.precio) } ?: "") }
    var stockTexto by remember { mutableStateOf(inicial?.stock?.toString() ?: "") }
    var categoria by remember {
        mutableStateOf(inicial?.let { p -> categorias.firstOrNull { it.id == p.categoria.id } })
    }
    var menuCategorias by remember { mutableStateOf(false) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorPrecio by remember { mutableStateOf<String?>(null) }
    var errorStock by remember { mutableStateOf<String?>(null) }
    var errorCategoria by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (categorias.isEmpty()) {
                    Text(
                        "No existen categorías. Creá una categoría antes de crear productos.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            errorNombre = null
                        },
                        label = { Text("Nombre del producto") },
                        singleLine = true,
                        isError = errorNombre != null,
                        supportingText = { errorNombre?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = precioTexto,
                        onValueChange = {
                            precioTexto = it
                            errorPrecio = null
                        },
                        label = { Text("Precio (\$)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorPrecio != null,
                        supportingText = { errorPrecio?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stockTexto,
                        onValueChange = {
                            stockTexto = it
                            errorStock = null
                        },
                        label = { Text("Stock") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorStock != null,
                        supportingText = { errorStock?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box {
                        OutlinedButton(
                            onClick = { menuCategorias = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = categoria?.nombre ?: "Seleccionar categoría",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = menuCategorias,
                            onDismissRequest = { menuCategorias = false }
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre) },
                                    onClick = {
                                        categoria = cat
                                        errorCategoria = null
                                        menuCategorias = false
                                    }
                                )
                            }
                        }
                    }
                    errorCategoria?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = categorias.isNotEmpty(),
                onClick = {
                    val precio = parseDecimal(precioTexto)
                    val stock = stockTexto.trim().toIntOrNull()

                    errorNombre = when {
                        nombre.isBlank() -> "El nombre es obligatorio"
                        nombre.trim().length > 60 -> "Máximo 60 caracteres"
                        nombreRepetido(nombre.trim()) -> "Ya existe un producto con ese nombre"
                        else -> null
                    }
                    errorPrecio = when {
                        precioTexto.isBlank() -> "El precio es obligatorio"
                        precio == null -> "Ingresá un número válido"
                        precio <= 0 -> "Debe ser mayor a 0"
                        else -> null
                    }
                    errorStock = when {
                        stockTexto.isBlank() -> "El stock es obligatorio"
                        stock == null -> "Ingresá un número entero"
                        stock < 0 -> "No puede ser negativo"
                        else -> null
                    }
                    errorCategoria = if (categoria == null) "Seleccioná una categoría" else null

                    val seleccionada = categoria
                    if (errorNombre == null && errorPrecio == null && errorStock == null &&
                        errorCategoria == null && precio != null && stock != null && seleccionada != null
                    ) {
                        onGuardar(nombre.trim(), precio, stock, seleccionada)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
