package services

import models.Promocion

class PromocionService {

    private val promociones = mutableListOf<Promocion>()
    private var siguientePromocionId = 1

    // Promociones de ejemplo para que la pantalla no inicie vacía (solo si no hay ninguna)
    fun cargarPromocionesIniciales(productoService: ProductoService) {
        if (promociones.isNotEmpty()) return
        val productos = productoService.obtenerProductos()

        productos.firstOrNull { it.nombre.startsWith("Detergente") }
            ?.let { agregarPromocion("En productos de limpieza", 20.0, it.id) }
        productos.firstOrNull { it.nombre.startsWith("Agua") }
            ?.let { agregarPromocion("En bebidas seleccionadas", 10.0, it.id) }
        productos.firstOrNull { it.nombre.startsWith("Cereal") }
            ?.let { agregarPromocion("En cereales", 15.0, it.id) }
    }

    fun obtenerPromociones(): List<Promocion> {
        return promociones
    }

    fun buscarPorId(id: Int): Promocion? {
        return promociones.find { it.id == id }
    }

    fun agregarPromocion(nombre: String, descuento: Double, productoId: Int): Promocion {
        val nuevaPromocion = Promocion(
            id = siguientePromocionId,
            nombre = nombre,
            descuento = descuento,
            productoId = productoId
        )
        promociones.add(nuevaPromocion)
        siguientePromocionId++
        return nuevaPromocion
    }

    // Edita nombre, descuento y producto de una promoción existente
    fun actualizarPromocion(id: Int, nombre: String, descuento: Double, productoId: Int): Boolean {
        val promocion = buscarPorId(id) ?: return false
        promocion.nombre = nombre
        promocion.descuento = descuento
        promocion.productoId = productoId
        return true
    }

    fun eliminarPromocion(promocion: Promocion): Boolean {
        return promociones.remove(promocion)
    }

    // Método para eliminar promociones cuando se borra un producto
    fun eliminarPorProductoId(productoId: Int) {
        promociones.removeIf { it.productoId == productoId }
    }
}