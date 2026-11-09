package services

import config.FirebaseProvider
import models.Categoria
import utils.Utils

class CategoriaService {
    private val db = FirebaseProvider.db
    private val utils = Utils()
    private val categorias = mutableListOf<Categoria>()
    private var siguienteCategoriaId = 1

    /*suspend fun cargarCategoriasIniciales() {
        if (categorias.isEmpty()) {
            agregarCategoria("Alimentos")
            agregarCategoria("Bebidas")
            agregarCategoria("Limpieza")
            agregarCategoria("Higiene personal")
        }
    }*/

    suspend fun obtenerCategorias(): List<Categoria> {
        val qsCategorias = db.collection("categorias").get()
        val categorias = qsCategorias.documents.map { document ->
            Categoria(
                id = document.id,
                nombre = document.get<String>("nombre") ?: ""
            )
        }
        return categorias
    }

    fun buscarPorId(id: Int): Int {
        return 1
    }

    suspend fun agregarCategoria(nombre: String): String {
        val slug = utils.slugify(nombre)
        val categoriaData = mapOf("idCategoria" to utils.slugify(slug), "nombre" to nombre)
        db.collection("categorias").document(slug).set(categoriaData)
        return nombre
    }

    fun eliminarCategoria(categoria: Categoria): Boolean {
        return categorias.remove(categoria)
    }
}