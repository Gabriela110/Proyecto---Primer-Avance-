package views

import services.*
import utils.ConsoleUtils

class ListaCompraView(
    private val listaCompraService: ListaCompraService,
    private val productoService: ProductoService,
    private val consoleUtils: ConsoleUtils
) {

    fun menuListaCompra() {
        var opcion: Int

        do {
            println("\n===== MENÚ PRESUPUESTO Y COMPRAS =====")
            println("1. Establecer presupuesto")
            println("2. Agregar producto a la lista")
            println("3. Modificar cantidad de un producto")
            println("4. Eliminar producto de la lista")
            println("5. Ver lista completa")
            println("6. Ver saldo disponible")
            println("7. Regresar")

            opcion = consoleUtils.leerEntero("Seleccione una opción: ")

            when (opcion) {
                1 -> establecerPresupuesto()
                2 -> agregarProducto()
                3 -> modificarCantidad()
                4 -> eliminarProducto()
                5 -> mostrarLista()
                6 -> println("\nSaldo disponible: $${"%.2f".format(listaCompraService.calcularSaldoDisponible())}")
                7 -> println("Regresando...")
                else -> println("Opción no válida.")
            }
        } while (opcion != 7)
    }

    private fun establecerPresupuesto() {
        try {
            val monto = consoleUtils.leerDoublePositivo("Nuevo presupuesto: ")
            listaCompraService.establecerPresupuesto(monto)
            println("Presupuesto actualizado correctamente.")
        } catch (e: PresupuestoInvalidoException) {
            println("Error: ${e.message}")
            Logger.registrarError(e.message ?: "Error al establecer presupuesto")
        } catch (e: Exception) {
            println("Ocurrió un error inesperado.")
            Logger.registrarError(e.toString())
        }
    }

    private fun agregarProducto() {
        val productosDisponibles = productoService.obtenerProductos()
        if (productosDisponibles.isEmpty()) {
            println("No hay productos registrados en el catálogo.")
            return
        }

        println("\n--- Catálogo de Productos ---")
        for (p in productosDisponibles) {
            println("ID: ${p.id} | Nombre: ${p.nombre} | Precio: $${p.precio}")
        }

        try {
            val id = consoleUtils.leerEntero("\nID del producto: ")
            val cantidad = consoleUtils.leerEntero("Cantidad: ")

            listaCompraService.agregarProducto(id, cantidad)
            println("Producto agregado correctamente.")
        } catch (e: PresupuestoExcedidoException) {
            println("🚫 ${e.message}")
            Logger.registrarError(e.message ?: "Presupuesto excedido al agregar producto")
        } catch (e: ProductoInvalidoException) {
            println("Error: ${e.message}")
            Logger.registrarError(e.message ?: "Error al agregar producto")
        } catch (e: ProductoNoEncontradoException) {
            println("Error: ${e.message}")
            Logger.registrarError(e.message ?: "Producto no encontrado")
        } catch (e: Exception) {
            println("Ocurrió un error inesperado.")
            Logger.registrarError(e.toString())
        }
    }

    private fun mostrarLista() {
        val elementos = listaCompraService.obtenerElementos()
        if (elementos.isEmpty()) {
            println("La lista de compras está vacía.")
            return
        }

        println("\n--- Tu Lista de Compras ---")
        var index = 1
        for ((producto, cantidad) in elementos) {
            val subtotal = producto.precio * cantidad
            println(
                "$index. ${producto.nombre} | " +
                        "Cantidad: $cantidad | " +
                        "Precio unitario: $${"%.2f".format(producto.precio)} | " +
                        "Subtotal: $${"%.2f".format(subtotal)}"
            )
            index++
        }
        println("-------------------------")
        println("Total: $${"%.2f".format(listaCompraService.calcularTotalLista())}")
        println("Saldo disponible: $${"%.2f".format(listaCompraService.calcularSaldoDisponible())}")
    }

    private fun modificarCantidad() {
        mostrarLista()
        val elementos = listaCompraService.obtenerElementos()
        if (elementos.isEmpty()) return

        try {
            val id = consoleUtils.leerEntero("\nID del producto a modificar: ")
            val nuevaCantidad = consoleUtils.leerEntero("Nueva cantidad: ")

            listaCompraService.modificarCantidad(id, nuevaCantidad)
            println("Cantidad actualizada correctamente.")
        } catch (e: PresupuestoExcedidoException) {
            println("🚫 ${e.message}")
            Logger.registrarError(e.message ?: "Presupuesto excedido al modificar cantidad")
        } catch (e: Exception) {
            println("Error: ${e.message}")
            Logger.registrarError(e.message ?: "Error al modificar cantidad")
        }
    }

    private fun eliminarProducto() {
        mostrarLista()
        val elementos = listaCompraService.obtenerElementos()
        if (elementos.isEmpty()) return

        try {
            val id = consoleUtils.leerEntero("\nID del producto a eliminar: ")
            listaCompraService.eliminarProducto(id)
            println("Producto eliminado correctamente.")
        } catch (e: ProductoNoEncontradoException) {
            println("Error: ${e.message}")
            Logger.registrarError(e.message ?: "Error al eliminar producto")
        } catch (e: Exception) {
            println("Ocurrió un error inesperado.")
            Logger.registrarError(e.toString())
        }
    }
}