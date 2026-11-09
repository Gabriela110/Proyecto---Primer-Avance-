package services

import config.FirebaseProvider
import models.Categoria
import models.Producto
import utils.Utils

class ProductoService {

    private val db = FirebaseProvider.db
    private val utils = Utils()
    private val productos = mutableListOf<Producto>()

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