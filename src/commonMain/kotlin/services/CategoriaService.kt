package services

import models.Categoria

class CategoriaService {

    private val categorias = mutableListOf<Categoria>()
    private var siguienteCategoriaId = 1

    fun cargarCategoriasIniciales() {
        if (categorias.isEmpty()) {
            agregarCategoria("Alimentos")
            agregarCategoria("Bebidas")
            agregarCategoria("Limpieza")
            agregarCategoria("Higiene personal")
        }
    }

    fun obtenerCategorias(): List<Categoria> {
        return categorias
    }

    fun buscarPorId(id: Int): Categoria? {
        return categorias.find { it.id == id }
    }

    fun agregarCategoria(nombre: String): Categoria {
        val nuevaCategoria = Categoria(siguienteCategoriaId, nombre)
        categorias.add(nuevaCategoria)
        siguienteCategoriaId++
        return nuevaCategoria
    }

    fun eliminarCategoria(categoria: Categoria): Boolean {
        return categorias.remove(categoria)
    }
}