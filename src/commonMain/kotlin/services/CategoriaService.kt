package services

import config.FirebaseProvider
import models.Categoria

class CategoriaService {
    private val db = FirebaseProvider.db

    private val categorias = mutableListOf<Categoria>()
    private var siguienteCategoriaId = 1

    suspend fun cargarCategoriasIniciales() {
        if (categorias.isEmpty()) {
            agregarCategoria("Alimentos")
            agregarCategoria("Bebidas")
            agregarCategoria("Limpieza")
            agregarCategoria("Higiene personal")
        }
    }

    fun obtenerCategorias(): List<Categoria> {
        return categorias.toList()
    }

    fun buscarPorId(id: Int): Categoria? {
        return categorias.find { it.id == id }
    }

    suspend fun agregarCategoria(nombre: String): Categoria {
        val categoriaData = mapOf("nombre" to nombre)
        db.collection("categorias").add(categoriaData)

        val nuevaCategoria = Categoria(siguienteCategoriaId, nombre)
        categorias.add(nuevaCategoria)
        siguienteCategoriaId++
        return nuevaCategoria
    }

    fun eliminarCategoria(categoria: Categoria): Boolean {
        return categorias.remove(categoria)
    }
}