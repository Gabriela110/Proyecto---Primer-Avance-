package services

import models.Categoria
import models.Producto

class ProductoService {

    private val productos = mutableListOf<Producto>()
    private var siguienteProductoId = 1

    fun cargarProductosIniciales(categoriaService: CategoriaService) {
        val categorias = categoriaService.obtenerCategorias()
        if (categorias.isNotEmpty() && productos.isEmpty()) {
            val catAlimentos = categorias[0]
            val catBebidas = if (categorias.size > 1) categorias[1] else catAlimentos

            agregarProducto("Arroz 1lb", 1.25, 50, catAlimentos)
            agregarProducto("Agua Embotellada 600ml", 0.75, 100, catBebidas)
        }
    }

    fun obtenerProductos(): List<Producto> {
        return productos
    }

    fun buscarPorId(id: Int): Producto? {
        return productos.find { it.id == id }
    }

    fun agregarProducto(nombre: String, precio: Double, stock: Int, categoria: Categoria): Producto {
        val nuevoProducto = Producto(
            id = siguienteProductoId,
            nombre = nombre,
            precio = precio,
            stock = stock,
            categoria = categoria
        )
        productos.add(nuevoProducto)
        siguienteProductoId++
        return nuevoProducto
    }

    fun eliminarProducto(producto: Producto): Boolean {
        return productos.remove(producto)
    }

    fun existeProductoEnCategoria(categoriaId: Int): Boolean {
        return productos.any { it.categoria.id == categoriaId }
    }
}