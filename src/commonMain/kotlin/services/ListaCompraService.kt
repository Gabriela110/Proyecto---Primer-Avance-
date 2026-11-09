package services

import config.FirebaseProvider
import models.Producto
import models.ItemCarrito
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

// Excepciones personalizadas
class ProductoInvalidoException(mensaje: String) : Exception(mensaje)
class ProductoNoEncontradoException(mensaje: String) : Exception(mensaje)
class PresupuestoInvalidoException(mensaje: String) : Exception(mensaje)
class PresupuestoExcedidoException(mensaje: String) : Exception(mensaje)

// Logger original
object Logger {
    private val archivoLog = File("errores.log")
    private val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun registrarError(mensaje: String) {
        val timestamp = LocalDateTime.now().format(formato)
        archivoLog.appendText("[$timestamp] ERROR: $mensaje\n")
    }
}

class ListaCompraService(
    private val productoService: ProductoService
) {
    private var presupuesto: Double = 0.0
    private val db = FirebaseProvider.db

    // Caché local en memoria sincronizada con Firestore
    private var elementosCache = mapOf<Producto, Int>()

    suspend fun obtenerPresupuesto(): Double {
        val currentUser = Firebase.auth.currentUser
        val qsPresupuesto = db.collection("presupuestos").where { "userId" equalTo currentUser?.uid }.get()
        var monto: Double = 0.0
        for (document in qsPresupuesto.documents) {
            monto = document.get<Double>("monto") ?: 0.0
        }
        presupuesto = monto
        return presupuesto
    }

    suspend fun obtenerElementos(): Map<Producto, Int> {
        val currentUser = Firebase.auth.currentUser ?: return emptyMap()
        val snapshot = db.collection("carrito")
            .where { "userId" equalTo currentUser.uid }
            .get()

        val mapaElementos = mutableMapOf<Producto, Int>()
        for (document in snapshot.documents) {
            val productoId = document.get<String>("productoId") ?: continue
            val cantidad = document.get<Int>("cantidad") ?: 0

            // Buscamos el producto en memoria
            var producto = productoService.buscarPorId(productoId)

            // Si no está en memoria, forzamos la obtención del catálogo
            if (producto == null) {
                productoService.obtenerProductos()
                producto = productoService.buscarPorId(productoId)
            }

            // Si aun así el servicio no lo mapea por ID, creamos un objeto Producto básico
            // utilizando el ID para que al menos se muestre en la interfaz y sume el total.
            val productoFinal = producto ?: run {
                try {
                    val productoDoc = db.collection("productos").document(productoId).get()
                    if (productoDoc.exists) {
                        Producto(
                            id = productoId,
                            nombre = productoDoc.get<String>("nombre") ?: "Producto",
                            precio = productoDoc.get<Double>("precio") ?: 0.0,
                            categoria = productoDoc.get<String>("categoria") ?: "",
                            stock = productoDoc.get<Int>("stock") ?: 0
                        )
                    } else {
                        Producto(
                            id = productoId,
                            nombre = "Producto ($productoId)",
                            precio = 0.0,
                            categoria = "",
                            stock = 0
                        )
                    }
                } catch (e: Exception) {
                    Producto(
                        id = productoId,
                        nombre = "Producto ($productoId)",
                        precio = 0.0,
                        categoria = "",
                        stock = 0
                    )
                }
            }

            if (cantidad > 0) {
                mapaElementos[productoFinal] = cantidad
            }
        }
        elementosCache = mapaElementos
        return elementosCache
    }

    suspend fun agregarProducto(productoId: String, cantidad: Int) {
        if (cantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val currentUser = Firebase.auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")

        // 1. Obtenemos el producto del catálogo para verificar stock y precio
        val productoDoc = db.collection("productos").document(productoId).get()
        if (!productoDoc.exists) {
            throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en el catálogo.")
        }

        val stockActual = productoDoc.get<Int>("stock") ?: 0
        val precio = productoDoc.get<Double>("precio") ?: 0.0
        val nombre = productoDoc.get<String>("nombre") ?: ""
        val categoria = productoDoc.get<String>("categoria") ?: ""

        // Validar que haya suficiente stock
        if (cantidad > stockActual) {
            throw Exception("No hay suficiente stock disponible. Stock actual: $stockActual")
        }

        val subtotalNuevo = precio * cantidad
        if (excederiaPresupuesto(subtotalNuevo)) {
            throw PresupuestoExcedidoException("No se puede agregar: excede tu presupuesto disponible.")
        }

        // 2. Guardamos o actualizamos en la colección "carrito"
        val docId = "${currentUser.uid}_$productoId"
        val itemData = mapOf(
            "userId" to currentUser.uid,
            "productoId" to productoId,
            "cantidad" to cantidad
        )
        db.collection("carrito").document(docId).set(itemData)

        // 3. Reducimos el stock en la colección "productos" (stock actual menos la cantidad ingresada)
        val nuevoStock = stockActual - cantidad
        db.collection("productos").document(productoId).update(mapOf("stock" to nuevoStock))

        // Refrescamos la vista local
        obtenerElementos()
    }
    suspend fun eliminarProducto(productoId: String) {
        val currentUser = Firebase.auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val producto = elementosCache.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        val docId = "${currentUser.uid}_$productoId"
        db.collection("carrito").document(docId).delete()
        obtenerElementos() // Refresca la caché local
    }

    suspend fun modificarCantidad(productoId: String, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val currentUser = Firebase.auth.currentUser ?: throw IllegalStateException("Usuario no autenticado")
        val producto = elementosCache.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        val cantidadActual = elementosCache[producto] ?: 0
        val diferenciaCantidad = nuevaCantidad - cantidadActual
        val subtotalAdicional = diferenciaCantidad * producto.precio

        if (diferenciaCantidad > 0 && excederiaPresupuesto(subtotalAdicional)) {
            throw PresupuestoExcedidoException(
                "No se puede aumentar la cantidad de '${producto.nombre}': el total quedaría en " +
                        "$${"%.2f".format(calcularTotalLista() + subtotalAdicional)}, que excede tu presupuesto de " +
                        "$${"%.2f".format(presupuesto)}."
            )
        }

        val docId = "${currentUser.uid}_$productoId"
        val itemActualizado = ItemCarrito(
            userId = currentUser.uid,
            productoId = productoId,
            cantidad = nuevaCantidad
        )

        db.collection("carrito").document(docId).set(itemActualizado)
        obtenerElementos() // Refresca la caché local
    }

    fun calcularTotalLista(): Double {
        return elementosCache.entries.sumOf { (producto, cantidad) -> producto.precio * cantidad }
    }

    suspend fun establecerPresupuesto(monto: Double) {
        val currentUser = Firebase.auth.currentUser
        if (monto < 0) {
            throw PresupuestoInvalidoException("El presupuesto no puede ser negativo.")
        }
        val userId = currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        val userEmail = currentUser.email
        val presupuestoData = mapOf("userId" to userId, "userEmail" to userEmail, "monto" to monto)

        db.collection("presupuestos").document(userId).set(presupuestoData)
        presupuesto = monto
    }

    suspend fun calcularSaldoDisponible(): Double {
        obtenerPresupuesto()
        return presupuesto - calcularTotalLista()
    }

    private fun excederiaPresupuesto(montoAdicional: Double): Boolean {
        return (calcularTotalLista() + montoAdicional) > presupuesto
    }
}