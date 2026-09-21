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
    private val productoService: ProductoService
) {
    // Mapa: Producto del catálogo como Clave, Cantidad (Int) como Valor
    private val elementos = mutableMapOf<Producto, Int>()
    private var presupuesto: Double = 0.0
    private var presupuestoEstablecido: Boolean = false

    fun obtenerElementos(): Map<Producto, Int> = elementos

    fun agregarProducto(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val producto = productoService.buscarPorId(productoId)
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId.")

        val subtotalNuevo = producto.precio * cantidad

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

    fun eliminarProducto(productoId: Int) {
        val producto = elementos.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        elementos.remove(producto)
    }

    fun modificarCantidad(productoId: Int, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val producto = elementos.keys.find { it.id == productoId }
            ?: throw ProductoNoEncontradoException("No se encontró el producto con ID $productoId en la lista.")

        val cantidadActual = elementos[producto] ?: 0
        val diferenciaCantidad = nuevaCantidad - cantidadActual
        val subtotalAdicional = diferenciaCantidad * producto.precio

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
        return elementos.entries.sumOf { (producto, cantidad) -> producto.precio * cantidad }
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