package services

import config.FirebaseProvider
import models.Producto
import utils.Utils

class ProductoService {

    private val db = FirebaseProvider.db
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
        this.productos.clear()
        this.productos.addAll(productos)
        return productos
    }

    fun buscarPorId(id: String): Producto? {
        return productos.find { it.id == id }
    }

    suspend fun agregarProducto(nombre: String, precio: Double, stock: Int, categoriaId: String): String {
        val slugCategoria = Utils.slugify(categoriaId)
        val productoData = mapOf(
            "nombre" to nombre,
            "precio" to precio,
            "stock" to stock,
            "categoria" to slugCategoria
        )
        db.collection("productos").add(productoData)
        return slugCategoria
    }

    // Edita un producto existente (se modifica el mismo objeto para que las demás pantallas lo vean)
    suspend fun actualizarProducto(id: String, nombre: String, precio: Double, stock: Int, categoriaId: String): Boolean {
        return try {
            val doc = db.collection("productos").document(id).get()
            if (!doc.exists) return false

            val slugCategoria = Utils.slugify(categoriaId)
            db.collection("productos").document(id).update(
                mapOf(
                    "nombre" to nombre,
                    "precio" to precio,
                    "stock" to stock,
                    "categoria" to slugCategoria
                )
            )

            val index = productos.indexOfFirst { it.id == id }
            if (index != -1) {
                productos[index] = productos[index].copy(
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    categoria = slugCategoria
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ignorarId permite editar sin chocar consigo mismo
    suspend fun existeNombre(nombre: String, ignorarId: String? = null): Boolean {
        val actuales = obtenerProductos()
        return actuales.any { it.nombre.equals(nombre.trim(), ignoreCase = true) && it.id != ignorarId }
    }

    suspend fun eliminarProducto(producto: Producto): Boolean {
        return try {
            db.collection("productos").document(producto.id).delete()
            productos.removeAll { it.id == producto.id }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun existeProductoEnCategoria(categoriaId: String): Boolean {
        val slugCategoria = Utils.slugify(categoriaId)
        val snapshot = db.collection("productos")
            .where { "categoria" equalTo slugCategoria }
            .get()
        return snapshot.documents.isNotEmpty()
    }
}