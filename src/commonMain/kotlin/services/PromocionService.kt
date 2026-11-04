package services

import models.Promocion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PromocionService {
    private val _promociones = MutableStateFlow<List<Promocion>>(emptyList())
    val promociones: StateFlow<List<Promocion>> = _promociones.asStateFlow()

    fun cargarPromocionesIniciales(productoService: ProductoService) {
        if (_promociones.value.isEmpty()) {
            val productos = productoService.obtenerProductos()
            if (productos.isNotEmpty()) {
                _promociones.value = listOf(
                    Promocion(id = "1", productoId = productos[0].id, porcentajeDescuento = 20.0, descripcion = "Descuento en Lácteos", activa = true)
                )
            }
        }
    }

    fun obtenerPromociones(): List<Promocion> = _promociones.value

    fun agregarPromocion(promocion: Promocion) {
        val nuevoId = (promociones.value.size + 1).toString()
        _promociones.value = _promociones.value + promocion.copy(id = nuevoId)
    }

    fun agregarPromocion(nombre: String, descuento: Double, productoId: String, descripcion: String = "") {
        val nuevoId = (promociones.value.size + 1).toString()
        val nueva = Promocion(
            id = nuevoId,
            nombre = nombre,
            productoId = productoId,
            porcentajeDescuento = descuento,
            descuento = descuento,
            descripcion = descripcion
        )
        _promociones.value = _promociones.value + nueva
    }

    fun agregarPromocion(nombre: String, descuento: Double, productoId: Int, descripcion: String = "") {
        agregarPromocion(nombre, descuento, productoId.toString(), descripcion)
    }

    fun actualizarPromocion(promocion: Promocion) {
        _promociones.value = _promociones.value.map { if (it.id == promocion.id) promocion else it }
    }

    fun actualizarPromocion(id: String, nombre: String, descuento: Double, productoId: String, descripcion: String = "") {
        _promociones.value = _promociones.value.map {
            if (it.id == id) {
                it.copy(
                    nombre = nombre,
                    porcentajeDescuento = descuento,
                    descuento = descuento,
                    productoId = productoId,
                    descripcion = descripcion
                )
            } else {
                it
            }
        }
    }

    fun actualizarPromocion(id: String, nombre: String, descuento: Double, productoId: Int, descripcion: String = "") {
        actualizarPromocion(id, nombre, descuento, productoId.toString(), descripcion)
    }

    fun eliminarPromocion(id: String): Boolean {
        val existe = _promociones.value.any { it.id == id }
        if (existe) {
            _promociones.value = _promociones.value.filterNot { it.id == id }
        }
        return existe
    }

    fun eliminarPromocion(promocion: Promocion): Boolean {
        return eliminarPromocion(promocion.id)
    }

    fun eliminarPorProductoId(productoId: String) {
        _promociones.value = _promociones.value.filterNot { it.productoId == productoId }
    }

    fun eliminarPorProductoId(productoId: Int) {
        eliminarPorProductoId(productoId.toString())
    }

    fun cambiarEstadoPromocion(id: String, activa: Boolean) {
        _promociones.value = _promociones.value.map {
            if (it.id == id) it.copy(activa = activa) else it
        }
    }
}
