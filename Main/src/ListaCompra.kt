import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProductoInvalidoException(mensaje: String) : Exception(mensaje)
class ProductoNoEncontradoException(mensaje: String) : Exception(mensaje)
class PresupuestoInvalidoException(mensaje: String) : Exception(mensaje)


object Logger {
    private val archivoLog = File("errores.log")
    private val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun registrarError(mensaje: String) {
        val timestamp = LocalDateTime.now().format(formato)
        archivoLog.appendText("[$timestamp] ERROR: $mensaje\n")
    }
}


data class Producto(
    val nombre: String,
    val precioUnitario: Double,
    var cantidad: Int
) {
    fun calcularSubtotal(): Double = precioUnitario * cantidad
}


class ListaCompra(private var presupuesto: Double = 0.0) {

    private val productos: MutableList<Producto> = mutableListOf()
    fun agregarProducto(nombre: String, precioUnitario: Double, cantidad: Int) {
        if (nombre.isBlank()) {
            throw ProductoInvalidoException("El nombre del producto no puede estar vacío.")
        }
        if (precioUnitario <= 0) {
            throw ProductoInvalidoException("El precio unitario debe ser mayor a 0.")
        }
        if (cantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }

        val existente = productos.find { it.nombre.equals(nombre, ignoreCase = true) }
        if (existente != null) {
            existente.cantidad += cantidad
        } else {
            productos.add(Producto(nombre, precioUnitario, cantidad))
        }

        verificarPresupuesto()
    }

    fun eliminarProducto(nombre: String) {
        val producto = buscarProducto(nombre)
        productos.remove(producto)
    }

    fun modificarCantidad(nombre: String, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            throw ProductoInvalidoException("La cantidad debe ser mayor a 0.")
        }
        val producto = buscarProducto(nombre)
        producto.cantidad = nuevaCantidad
        verificarPresupuesto()
    }

    private fun buscarProducto(nombre: String): Producto {
        return productos.find { it.nombre.equals(nombre, ignoreCase = true) }
            ?: throw ProductoNoEncontradoException("No se encontró el producto '$nombre' en la lista.")
    }


    fun calcularSubtotal(nombre: String): Double {
        val producto = buscarProducto(nombre)
        return producto.calcularSubtotal()
    }

    fun calcularTotalLista(): Double {
        return productos.sumOf { it.calcularSubtotal() }
    }


    fun establecerPresupuesto(monto: Double) {
        if (monto < 0) {
            throw PresupuestoInvalidoException("El presupuesto no puede ser negativo.")
        }
        presupuesto = monto
    }

    fun calcularSaldoDisponible(): Double {
        return presupuesto - calcularTotalLista()
    }

    fun excedePresupuesto(): Boolean {
        return calcularTotalLista() > presupuesto
    }

    private fun verificarPresupuesto() {
        if (excedePresupuesto()) {
            mostrarAdvertencia()
        }
    }

    fun mostrarAdvertencia() {
        println("⚠️  ADVERTENCIA: Has excedido tu presupuesto de $${"%.2f".format(presupuesto)}")
        println("    Total actual: $${"%.2f".format(calcularTotalLista())}")
        println("    Exceso: $${"%.2f".format(calcularTotalLista() - presupuesto)}")
    }

    // --- Visualización ---

    fun mostrarLista() {
        if (productos.isEmpty()) {
            println("La lista de compras está vacía.")
            return
        }
        println("\n--- Lista de Compras ---")
        productos.forEachIndexed { index, producto ->
            println(
                "${index + 1}. ${producto.nombre} | " +
                        "Cantidad: ${producto.cantidad} | " +
                        "Precio unitario: $${"%.2f".format(producto.precioUnitario)} | " +
                        "Subtotal: $${"%.2f".format(producto.calcularSubtotal())}"
            )
        }
        println("-------------------------")
        println("Total: $${"%.2f".format(calcularTotalLista())}")
        println("Presupuesto: $${"%.2f".format(presupuesto)}")
        println("Saldo disponible: $${"%.2f".format(calcularSaldoDisponible())}")
    }
}



fun main() {
    val lista = ListaCompra()

    while (true) {
        println(
            """
            |
            |===== MENÚ LISTA DE COMPRAS =====
            |1. Agregar producto
            |2. Eliminar producto
            |3. Modificar cantidad
            |4. Establecer presupuesto
            |5. Ver lista completa
            |6. Ver saldo disponible
            |0. Salir
            |==================================
            """.trimMargin()
        )
        print("Selecciona una opción: ")

        when (readLine()?.trim()) {
            "1" -> {
                try {
                    print("Nombre del producto: ")
                    val nombre = readLine().orEmpty()
                    print("Precio unitario: ")
                    val precio = readLine()?.toDoubleOrNull()
                        ?: throw ProductoInvalidoException("Precio inválido, debe ser un número.")
                    print("Cantidad: ")
                    val cantidad = readLine()?.toIntOrNull()
                        ?: throw ProductoInvalidoException("Cantidad inválida, debe ser un número entero.")

                    lista.agregarProducto(nombre, precio, cantidad)
                    println("Producto agregado correctamente.")
                } catch (e: ProductoInvalidoException) {
                    println("Error: ${e.message}")
                    Logger.registrarError(e.message ?: "Error desconocido al agregar producto")
                } catch (e: Exception) {
                    println("Ocurrió un error inesperado.")
                    Logger.registrarError(e.toString())
                }
            }

            "2" -> {
                try {
                    print("Nombre del producto a eliminar: ")
                    val nombre = readLine().orEmpty()
                    lista.eliminarProducto(nombre)
                    println("Producto eliminado correctamente.")
                } catch (e: ProductoNoEncontradoException) {
                    println("Error: ${e.message}")
                    Logger.registrarError(e.message ?: "Error al eliminar producto")
                }
            }

            "3" -> {
                try {
                    print("Nombre del producto a modificar: ")
                    val nombre = readLine().orEmpty()
                    print("Nueva cantidad: ")
                    val cantidad = readLine()?.toIntOrNull()
                        ?: throw ProductoInvalidoException("Cantidad inválida.")
                    lista.modificarCantidad(nombre, cantidad)
                    println("Cantidad actualizada correctamente.")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    Logger.registrarError(e.message ?: "Error al modificar cantidad")
                }
            }

            "4" -> {
                try {
                    print("Nuevo presupuesto: ")
                    val monto = readLine()?.toDoubleOrNull()
                        ?: throw PresupuestoInvalidoException("Monto inválido.")
                    lista.establecerPresupuesto(monto)
                    println("Presupuesto actualizado correctamente.")
                } catch (e: PresupuestoInvalidoException) {
                    println("Error: ${e.message}")
                    Logger.registrarError(e.message ?: "Error al establecer presupuesto")
                }
            }

            "5" -> lista.mostrarLista()

            "6" -> println("Saldo disponible: $${"%.2f".format(lista.calcularSaldoDisponible())}")

            "0" -> {
                println("Saliendo del sistema...")
                return
            }

            else -> println("Opción no válida, intenta de nuevo.")
        }
    }
}