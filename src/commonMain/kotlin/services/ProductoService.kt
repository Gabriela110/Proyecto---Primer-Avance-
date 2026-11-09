package services

import config.FirebaseProvider
import models.Categoria
import models.Producto
import utils.Utils

class ProductoService {

    private val db = FirebaseProvider.db
    private val utils = Utils()
    private val productos = mutableListOf<Producto>()
    private var siguienteProductoId = 1

   /*fun cargarProductosIniciales(categoriaService: CategoriaService) {
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
    }*/

    //fun obtenerProductos(): List<Producto> {
      //  return productos.toList()
    //}
    suspend fun obtenerProductos(): List<Producto> {
        val qsProductos = db.collection("productos").get()
        val productos = qsProductos.documents.map { document ->
            Producto(
                id = document.id,
                nombre = document.get<String>("nombre") ?: "",
                precio = document.get<Double>("precio") ?: 0.0,
                stock = document.get<Int>("stock") ?: 0,
                categoria = document.get<String>("categoria") ?: ""
            )
        }
        return productos
    }
    fun buscarPorId(id: String): Producto? {
        return productos.find{ it.id == id }
    }

    /*fun agregarProducto(nombre: String, precio: Double, stock: Int, categoria: Categoria): Producto {
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
    }*/

    suspend fun agregarProducto(nombre: String, precio: Double, stock: Int, categoria: String): String {
        val productoData = mapOf("nombre" to nombre, "precio" to precio, "stock" to stock, "categoria" to categoria)
        db.collection("productos").add(productoData)
        return nombre
    }

    fun eliminarProducto(producto: Producto): Boolean {
        return productos.remove(producto)
    }

    //fun existeProductoEnCategoria(categoriaId: Int): Boolean {
      //  return productos.any { it.categoria.id == categoriaId }
   // }
}