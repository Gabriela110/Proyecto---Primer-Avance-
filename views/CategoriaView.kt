package views

import models.Categoria
import services.CategoriaService
import utils.ConsoleUtils

class CategoriaView(
    private val categoriaService: CategoriaService,
    private val consoleUtils: ConsoleUtils
){

    fun menuCategorias() {
        var opcion: Int

        do {
            println("\n========== CATEGORÍAS ==========")
            println("1. Crear categoría")
            println("2. Listar categorías")
            println("3. Actualizar categoría")
            println("4. Eliminar categoría")
            println("5. Regresar")

            opcion = consoleUtils.leerEntero("Seleccione una opción: ")

            when (opcion) {
                1 -> crearCategoria()
                2 -> listarCategorias()
                3 -> actualizarCategoria()
                4 -> eliminarCategoria()
                5 -> println("Regresando...")
                else -> println("Opción inválida.")
            }

        } while (opcion != 5)
    }

    fun crearCategoria() {
        println("\n========== CREAR CATEGORÍA ==========")

        val nombre = consoleUtils.leerTexto("Nombre de la categoría: ")
        categoriaService.agregarCategoria(nombre)

        println("Categoría creada correctamente.")
    }

    fun listarCategorias() {
        println("\n========== LISTA DE CATEGORÍAS ==========")

        val lista = categoriaService.obtenerCategorias()

        if (lista.isEmpty()) {
            println("No hay categorías registradas.")
            return
        }

        for (categoria in lista) {
            println("----------------------------------------")
            println("ID: ${categoria.id}")
            println("Nombre: ${categoria.nombre}")
        }
    }

    fun actualizarCategoria() {
        listarCategorias()

        if (categoriaService.obtenerCategorias().isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID de la categoría: ")
        val categoria = categoriaService.buscarPorId(id)

        if (categoria == null) {
            println("Categoría no encontrada.")
            return
        }

        val nuevoNombre = consoleUtils.leerTexto("Nuevo nombre: ")
        categoria.nombre = nuevoNombre

        println("Categoría actualizada correctamente.")
    }

    fun eliminarCategoria() {
        listarCategorias()

        if (categoriaService.obtenerCategorias().isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID de la categoría: ")
        val categoria = categoriaService.buscarPorId(id)

        if (categoria == null) {
            println("Categoría no encontrada.")
            return
        }

        // Se elimina la categoría a través del servicio
        categoriaService.eliminarCategoria(categoria)
        println("Categoría eliminada correctamente.")
    }
}