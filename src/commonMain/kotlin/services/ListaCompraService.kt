package services

import models.Producto
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Excepciones personalizadas originales
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
    private val productoService: ProductoService,
    // Opcional: si se entrega, la lista aplica las promociones vigentes a los precios
    private val promocionService: PromocionService? = null
) {
    // Mapa: Producto del catálogo como Clave, Cantidad (Int) como Valor
    private val elementos = mutableMapOf<Producto, Int>()
    private var presupuesto: Double = 0.0
    private var presupuestoEstablecido: Boolean = false

    fun obtenerElementos(): Map<Producto, Int> = elementos

    fun agregarProducto(productoId: String, cantidad: Int) {
        if (cantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val producto = productoService.buscarPorId(productoId)
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId.")

        val enLista = elementos[producto] ?: 0
        if (enLista + cantidad > producto.stock) {
            throw ProductoInvalidoException(
                "Stock insuficiente de '${producto.nombre}': hay ${producto.stock} disponibles y ya tenés $enLista en tu lista."
            )
        }

        val subtotalNuevo = precioUnitario(producto) * cantidad

        if (excederiaPresupuesto(subtotalNuevo)) {
            throw PresupuestoExcedidoException(
                "No se puede agregar '${producto.nombre}': el total quedaría en $${"%.2f".format(calcularTotalLista() + subtotalNuevo)}, " +
                        "que excede tu presupuesto de $${"%.2f".format(presupuesto)}. " +
                        "Saldo disponible actual: $${"%.2f".format(calcularSaldoDisponible())}"
            )
        }

        val cantidadActual = elementos[producto] ?: 0
        elementos[producto] = cantidadActual + cantidad
    }

    fun eliminarProducto(productoId: String) {
        val producto = elementos.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        elementos.remove(producto)
    }

    fun modificarCantidad(productoId: String, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val producto = elementos.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        if (nuevaCantidad > producto.stock) {
            throw ProductoInvalidoException(
                "Stock insuficiente de '${producto.nombre}': solo hay ${producto.stock} disponibles."
            )
        }

        val cantidadActual = elementos[producto] ?: 0
        val diferenciaCantidad = nuevaCantidad - cantidadActual
        val subtotalAdicional = diferenciaCantidad * precioUnitario(producto)

        if (diferenciaCantidad > 0 && excederiaPresupuesto(subtotalAdicional)) {
            throw PresupuestoExcedidoException(
                "No se puede aumentar la cantidad de '${producto.nombre}': el total quedaría en " +
                        "$${"%.2f".format(calcularTotalLista() + subtotalAdicional)}, que excede tu presupuesto de " +
                        "$${"%.2f".format(presupuesto)}."
            )
        }

        elementos[producto] = nuevaCantidad
    }

    fun calcularTotalLista(): Double {
        return elementos.entries.sumOf { (producto, cantidad) -> precioUnitario(producto) * cantidad }
    }

    // Mayor descuento (%) vigente para el producto, o 0.0 si no tiene promoción
    fun descuentoDe(producto: Producto): Double {
        return promocionService?.obtenerPromociones()
            ?.filter { it.productoId == producto.id }
            ?.maxOfOrNull { it.descuento } ?: 0.0
    }

    // Precio que realmente se cobra en la lista (con la promoción aplicada)
    fun precioUnitario(producto: Producto): Double {
        return producto.precio * (1 - descuentoDe(producto) / 100.0)
    }

    fun obtenerPresupuesto(): Double = presupuesto

    fun hayPresupuesto(): Boolean = presupuestoEstablecido

    // Quita un producto de la lista si está (se usa cuando el administrador lo elimina del catálogo)
    fun quitarSiExiste(productoId: String) {
        elementos.keys.find { it.id == productoId }?.let { elementos.remove(it) }
    }

    // Producto es un data class mutable: si el administrador lo edita, su hash cambia y el mapa
    // se desordena. Este método reconstruye el mapa para que las búsquedas sigan funcionando.
    fun reindexar() {
        val copia = elementos.entries.map { it.key to it.value }
        elementos.clear()
        copia.forEach { (producto, cantidad) -> elementos[producto] = cantidad }
    }

    fun establecerPresupuesto(monto: Double) {
        if (monto < 0) {
            throw PresupuestoInvalidoException("El presupuesto no puede ser negativo.")
        }
        presupuesto = monto
        presupuestoEstablecido = true
    }

    fun calcularSaldoDisponible(): Double {
        return presupuesto - calcularTotalLista()
    }

    private fun excederiaPresupuesto(montoAdicional: Double): Boolean {
        if (!presupuestoEstablecido) return false
        return (calcularTotalLista() + montoAdicional) > presupuesto
    }
}
