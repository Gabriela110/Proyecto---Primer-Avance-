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
        return categorias.toList()
    }

    fun buscarPorId(id: String): Categoria? {
        return categorias.find { it.id == id }
    }

    fun agregarCategoria(nombre: String): Categoria {
        val nuevaCategoria = Categoria(id = siguienteCategoriaId.toString(), nombre = nombre)
        categorias.add(nuevaCategoria)
        siguienteCategoriaId++
        return nuevaCategoria
    }

    fun actualizarCategoria(id: String, nombre: String): Boolean {
        val index = categorias.indexOfFirst { it.id == id }
        if (index == -1) return false
        categorias[index] = categorias[index].copy(nombre = nombre)
        return true
    }

    // ¿Ya hay una categoría con ese nombre? (ignorarId permite editar sin chocar consigo misma)
    fun existeNombre(nombre: String, ignorarId: String? = null): Boolean {
        return categorias.any { it.nombre.equals(nombre.trim(), ignoreCase = true) && it.id != ignorarId }
    }

    fun eliminarCategoria(categoria: Categoria): Boolean {
        return categorias.remove(categoria)
    }
}
