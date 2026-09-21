package services

import models.Promocion

class PromocionService {

    private val promociones = mutableListOf<Promocion>()
    private var siguientePromocionId = 1

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

    fun eliminarPromocion(promocion: Promocion): Boolean {
        return promociones.remove(promocion)
    }

    // Método para eliminar promociones cuando se borra un producto
    fun eliminarPorProductoId(productoId: Int) {
        promociones.removeIf { it.productoId == productoId }
    }
}