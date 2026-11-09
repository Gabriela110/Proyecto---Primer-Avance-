package views

import services.ProductoService
import services.PromocionService
import utils.ConsoleUtils

class PromocionView(
    private val promocionService: PromocionService,
    private val productoService: ProductoService,
    private val consoleUtils: ConsoleUtils
) {

    fun menuPromociones() {
        var opcion: Int

        do {
            println("\n========== PROMOCIONES ==========")
            println("1. Crear promoción")
            println("2. Listar promociones")
            println("3. Actualizar promoción")
            println("4. Eliminar promoción")
            println("5. Regresar")

            opcion = consoleUtils.leerEntero("Seleccione una opción: ")

            when (opcion) {
                1 -> crearPromocion()
                2 -> listarPromociones()
                3 -> actualizarPromocion()
                4 -> eliminarPromocion()
                5 -> println("Regresando...")
                else -> println("Opción inválida.")
            }

        } while (opcion != 5)
    }

    fun crearPromocion() {
        println("\n========== CREAR PROMOCIÓN ==========")

        val productos = productoService.obtenerProductos()

        if (productos.isEmpty()) {
            println("Debe crear al menos un producto primero.")
            return
        }

        val nombre = consoleUtils.leerTexto("Nombre de la promoción: ")
        val descuento = consoleUtils.leerDescuento()

        println("\n--- Productos disponibles ---")
        for (prod in productos) {
            println("ID: ${prod.id} | Nombre: ${prod.nombre} | Precio: $${prod.precio}")
        }

        val productoId = consoleUtils.leerEntero("\nID del producto: ")
        val producto = productoService.buscarPorId(productoId)

        if (producto == null) {
            println("El producto no existe.")
            return
        }

        promocionService.agregarPromocion(nombre, descuento, productoId)
        println("Promoción creada correctamente.")
    }

    fun listarPromociones() {
        println("\n========== LISTA DE PROMOCIONES ==========")

        val promociones = promocionService.obtenerPromociones()

        if (promociones.isEmpty()) {
            println("No hay promociones registradas.")
            return
        }

        for (promocion in promociones) {
            val producto = productoService.buscarPorId(promocion.productoId)

            println("----------------------------------------")
            println("ID: ${promocion.id}")
            println("Nombre: ${promocion.nombre}")
            println("Descuento: ${promocion.descuento}%")
            println("Producto: ${producto?.nombre ?: "Producto eliminado"}")
        }
    }

    fun actualizarPromocion() {
        listarPromociones()

        val promociones = promocionService.obtenerPromociones()
        if (promociones.isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID de la promoción: ")
        val promocion = promocionService.buscarPorId(id)

        if (promocion == null) {
            println("Promoción no encontrada.")
            return
        }

        val nuevoNombre = consoleUtils.leerTexto("Nuevo nombre: ")
        val nuevoDescuento = consoleUtils.leerDescuento()

        promocion.nombre = nuevoNombre
        promocion.descuento = nuevoDescuento

        println("Promoción actualizada correctamente.")
    }

    fun eliminarPromocion() {
        listarPromociones()

        val promociones = promocionService.obtenerPromociones()
        if (promociones.isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID de la promoción: ")
        val promocion = promocionService.buscarPorId(id)

        if (promocion == null) {
            println("Promoción no encontrada.")
            return
        }

        promocionService.eliminarPromocion(promocion)
        println("Promoción eliminada correctamente.")
    }
}