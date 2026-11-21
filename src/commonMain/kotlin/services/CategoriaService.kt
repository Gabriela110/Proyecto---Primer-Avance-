package services

import config.FirebaseProvider
import models.Categoria
import utils.Utils

class CategoriaService {
    private val db = FirebaseProvider.db
    private val categorias = mutableListOf<Categoria>()

    suspend fun obtenerCategorias(): List<Categoria> {
        val qsCategorias = db.collection("categorias").get()
        val categorias = qsCategorias.documents.map { document ->
            Categoria(
                id = document.id,
                nombre = document.get<String>("nombre") ?: ""
            )
        }
        this.categorias.clear()
        this.categorias.addAll(categorias)
        return categorias
    }

    suspend fun agregarCategoria(nombre: String): String {
        val slug = Utils.slugify(nombre)
        val categoriaData = mapOf("idCategoria" to Utils.slugify(slug), "nombre" to nombre)
        db.collection("categorias").document(slug).set(categoriaData)
        return nombre
    }

    //ignorarId permite editar sin chocar consigo misma
    suspend fun existeNombre(nombre: String, ignorarId: String? = null): Boolean {
        val actuales = obtenerCategorias()
        return actuales.any { it.nombre.equals(nombre.trim(), ignoreCase = true) && it.id != ignorarId }
    }

    suspend fun actualizarCategoria(id: String, nombre: String): Boolean {
        return try {
            val doc = db.collection("categorias").document(id).get()
            if (!doc.exists) return false

            db.collection("categorias").document(id).update(mapOf("nombre" to nombre))
            val index = categorias.indexOfFirst { it.id == id }
            if (index != -1) {
                categorias[index] = categorias[index].copy(nombre = nombre)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun eliminarCategoria(categoria: Categoria): Boolean {
        return try {
            db.collection("categorias").document(categoria.id).delete()
            categorias.removeAll { it.id == categoria.id }
            true
        } catch (e: Exception) {
            false
        }
    }
}