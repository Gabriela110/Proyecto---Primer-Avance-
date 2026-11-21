package ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import models.Categoria
import models.Producto
import services.CategoriaService
import services.ProductoService
import ui.components.CampoBusqueda
import ui.components.EncabezadoVerde

/*
 * Gestión de categorías (administrador): ver, buscar, crear, editar y eliminar.
 * No deja eliminar una categoría que todavía tiene productos.
 */
@Composable
fun CategoryManagementScreen(
    categoriaService: CategoriaService,
    productoService: ProductoService,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var categorias by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaEnEdicion by remember { mutableStateOf<Categoria?>(null) }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun actualizarLista() {
        coroutineScope.launch {
            categorias = categoriaService.obtenerCategorias()
            productos = productoService.obtenerProductos()
        }
    }

    LaunchedEffect(Unit) {
        actualizarLista()
    }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    fun cantidadDeProductos(categoria: Categoria) = productos.count { it.categoria == categoria.id }

    val texto = busqueda.trim()
    val filtradas = categorias.filter { texto.isEmpty() || it.nombre.contains(texto, ignoreCase = true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { EncabezadoVerde(titulo = "Gestionar categorías", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    categoriaEnEdicion = null
                    mostrarFormulario = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva categoría") }
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
                placeholder = "Buscar categorías...",
                modifier = Modifier.padding(16.dp)
            )

            if (filtradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (categorias.isEmpty()) {
                            "Aún no hay categorías. Tocá «Nueva categoría» para crear la primera."
                        } else {
                            "No encontramos categorías con esa búsqueda."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtradas, key = { it.id }) { categoria ->
                        val cantidad = cantidadDeProductos(categoria)
                        TarjetaCategoria(
                            categoria = categoria,
                            cantidadProductos = cantidad,
                            onEditar = {
                                categoriaEnEdicion = categoria
                                mostrarFormulario = true
                            },
                            onEliminar = {
                                if (cantidad > 0) {
                                    mensaje = "No se puede eliminar \"${categoria.nombre}\": tiene $cantidad " +
                                            (if (cantidad == 1) "producto" else "productos") +
                                            ". Eliminá o cambiá de categoría esos productos primero."
                                } else {
                                    categoriaAEliminar = categoria
                                }
                            }
                        )
                    }
                }
            }
        }

        // ---- Crear / editar ----
        if (mostrarFormulario) {
            FormularioCategoria(
                inicial = categoriaEnEdicion,
                nombreRepetido = { nombre -> categoriaService.existeNombre(nombre, categoriaEnEdicion?.id) },
                onGuardar = { nombre ->
                    val editando = categoriaEnEdicion
                    coroutineScope.launch {
                        if (editando == null) {
                            categoriaService.agregarCategoria(nombre)
                            mensaje = "Categoría creada correctamente."
                        } else {
                            val actualizada = categoriaService.actualizarCategoria(editando.id, nombre)
                            mensaje = if (actualizada) {
                                "Categoría actualizada correctamente."
                            } else {
                                "No se pudo actualizar la categoría."
                            }
                        }
                        actualizarLista()
                    }
                    mostrarFormulario = false
                    categoriaEnEdicion = null
                },
                onCancelar = {
                    mostrarFormulario = false
                    categoriaEnEdicion = null
                }
            )
        }

        // ---- Eliminar (con confirmación) ----
        categoriaAEliminar?.let { categoria ->
            AlertDialog(
                onDismissRequest = { categoriaAEliminar = null },
                title = { Text("Eliminar categoría") },
                text = { Text("¿Seguro que querés eliminar \"${categoria.nombre}\"? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                mensaje = if (categoriaService.eliminarCategoria(categoria)) {
                                    "Categoría eliminada."
                                } else {
                                    "No se pudo eliminar la categoría."
                                }
                                actualizarLista()
                            }
                            categoriaAEliminar = null
                        }
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { categoriaAEliminar = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun TarjetaCategoria(
    categoria: Categoria,
    cantidadProductos: Int,
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (cantidadProductos) {
                        0 -> "Sin productos"
                        1 -> "1 producto"
                        else -> "$cantidadProductos productos"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar categoría")
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar categoría",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/* Formulario para crear (inicial = null) o renombrar una categoría. */
@Composable
private fun FormularioCategoria(
    inicial: Categoria?,
    nombreRepetido: suspend (String) -> Boolean,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(inicial?.nombre ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "Nueva categoría" else "Editar categoría") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    error = null
                },
                label = { Text("Nombre de la categoría") },
                singleLine = true,
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val limpio = nombre.trim()
                when {
                    nombre.isBlank() -> error = "El nombre es obligatorio"
                    limpio.length > 40 -> error = "Máximo 40 caracteres"
                    else -> coroutineScope.launch {
                        if (nombreRepetido(limpio)) {
                            error = "Ya existe una categoría con ese nombre"
                        } else {
                            onGuardar(limpio)
                        }
                    }
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}