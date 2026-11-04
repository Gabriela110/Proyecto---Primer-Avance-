package ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import models.Categoria
import services.CategoriaService
import services.ProductoService
import ui.components.CampoBusqueda
import ui.components.EncabezadoVerde

/**
 * Gestión de categorías (administrador): ver, buscar, crear, editar y eliminar.
 * No deja eliminar una categoría que todavía tiene productos.
 */
@Composable
fun CategoryManagementScreen(
    categoriaService: CategoriaService,
    productoService: ProductoService,
    onBack: () -> Unit
) {
    var busqueda by rememberSaveable { mutableStateOf("") }

    // Los servicios guardan listas simples (no observables): este contador refresca la pantalla
    var version by remember { mutableStateOf(0) }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaEnEdicion by remember { mutableStateOf<Categoria?>(null) }
    var categoriaAEliminar by remember { mutableStateOf<Categoria?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensaje by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            mensaje = null
        }
    }

    val categorias = version.let { categoriaService.obtenerCategorias() }
    val productos = productoService.obtenerProductos()
    fun cantidadDeProductos(categoria: Categoria) = productos.count { it.categoria.id == categoria.id }

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
    }

    // ---- Crear / editar ----
    if (mostrarFormulario) {
        FormularioCategoria(
            inicial = categoriaEnEdicion,
            nombreRepetido = { nombre -> categoriaService.existeNombre(nombre, categoriaEnEdicion?.id) },
            onGuardar = { nombre ->
                val editando = categoriaEnEdicion
                if (editando == null) {
                    categoriaService.agregarCategoria(nombre)
                    mensaje = "Categoría creada correctamente."
                } else {
                    // Los productos apuntan a este mismo objeto, así que ven el nuevo nombre
                    categoriaService.actualizarCategoria(editando.id, nombre)
                    mensaje = "Categoría actualizada correctamente."
                }
                version += 1
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
                        mensaje = if (categoriaService.eliminarCategoria(categoria)) {
                            "Categoría eliminada."
                        } else {
                            "No se pudo eliminar la categoría."
                        }
                        version += 1
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

/** Formulario para crear (inicial = null) o renombrar una categoría. */
@Composable
private fun FormularioCategoria(
    inicial: Categoria?,
    nombreRepetido: (String) -> Boolean,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
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
                error = when {
                    nombre.isBlank() -> "El nombre es obligatorio"
                    nombre.trim().length > 40 -> "Máximo 40 caracteres"
                    nombreRepetido(nombre.trim()) -> "Ya existe una categoría con ese nombre"
                    else -> null
                }
                if (error == null) onGuardar(nombre.trim())
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
