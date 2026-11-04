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

            // Productos extra para que las promociones de ejemplo tengan de dónde salir
            val catLimpieza = categorias.getOrElse(2) { catAlimentos }
            agregarProducto("Detergente líquido 1L", 3.50, 40, catLimpieza)
            agregarProducto("Cereal de avena 500g", 2.40, 30, catAlimentos)
        }
    }

    fun obtenerProductos(): List<Producto> {
        return productos.toList()
    }

    fun buscarPorId(id: String): Producto? {
        return productos.find { it.id == id }
    }

    fun agregarProducto(nombre: String, precio: Double, stock: Int, categoria: Categoria): Producto {
        val nuevoProducto = Producto(
            id = siguienteProductoId.toString(),
            nombre = nombre,
            precio = precio,
            categoriaId = categoria.id,
            categoria = categoria,
            stock = stock
        )
        productos.add(nuevoProducto)
        siguienteProductoId++
        return nuevoProducto
    }

    // Edita un producto existente (se modifica el mismo objeto para que las demás pantallas lo vean)
    fun actualizarProducto(id: String, nombre: String, precio: Double, stock: Int, categoria: Categoria): Boolean {
        val index = productos.indexOfFirst { it.id == id }
        if (index == -1) return false
        val productoActual = productos[index]
        productos[index] = productoActual.copy(
            nombre = nombre,
            precio = precio,
            stock = stock,
            categoriaId = categoria.id,
            categoria = categoria
        )
        return true
    }

    // ¿Ya hay un producto con ese nombre? (ignorarId permite editar sin chocar consigo mismo)
    fun existeNombre(nombre: String, ignorarId: String? = null): Boolean {
        return productos.any { it.nombre.equals(nombre.trim(), ignoreCase = true) && it.id != ignorarId }
    }

    fun eliminarProducto(producto: Producto): Boolean {
        return productos.remove(producto)
    }

    fun existeProductoEnCategoria(categoriaId: String): Boolean {
        return productos.any { it.categoria.id == categoriaId || it.categoriaId == categoriaId }
    }
}
