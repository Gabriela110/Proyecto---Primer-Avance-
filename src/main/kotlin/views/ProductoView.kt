package views

import services.CategoriaService
import services.ProductoService
import utils.ConsoleUtils

class ProductoView(
    private val productoService: ProductoService,
    private val categoriaService: CategoriaService,
    private val consoleUtils: ConsoleUtils
){

    fun menuProductos() {
        var opcion: Int
        do {
            println("\n========== PRODUCTOS ==========")
            println("1. Crear producto")
            println("2. Listar productos")
            println("3. Actualizar producto")
            println("4. Eliminar producto")
            println("5. Regresar")

            opcion = consoleUtils.leerEntero("Seleccione una opción: ")

            when (opcion) {
                1 -> crearProducto()
                2 -> listarProductos()
                3 -> actualizarProducto()
                4 -> eliminarProducto()
                5 -> println("Regresando...")
                else -> println("Opción inválida.")
            }

        } while (opcion != 5)
    }

    fun crearProducto() {
        println("\n========== CREAR PRODUCTO ==========")

        val categorias = categoriaService.obtenerCategorias()
        if (categorias.isEmpty()) {
            println("No existen categorías. Cree una categoría primero.")
            return
        }

        val nombre = consoleUtils.leerTexto("Nombre del producto: ")
        val precio = consoleUtils.leerDoublePositivo("Precio: ")
        val stock = consoleUtils.leerEnteroNoNegativo("Stock: ")

        println("\n--- Categorías disponibles ---")
        for (cat in categorias) {
            println("ID: ${cat.id} | Nombre: ${cat.nombre}")
        }

        val categoriaId = consoleUtils.leerEntero("Ingrese el ID de la categoría: ")
        val categoria = categoriaService.buscarPorId(categoriaId)

        if (categoria == null) {
            println("La categoría no existe.")
            return
        }

        productoService.agregarProducto(nombre, precio, stock, categoria)
        println("Producto creado correctamente.")
    }

    fun listarProductos() {
        println("\n========== LISTA DE PRODUCTOS ==========")

        val productos = productoService.obtenerProductos()

        if (productos.isEmpty()) {
            println("No hay productos registrados.")
            return
        }

        for (producto in productos) {
            println("----------------------------------------")
            println("ID: ${producto.id}")
            println("Nombre: ${producto.nombre}")
            println("Precio: $${producto.precio}")
            println("Stock: ${producto.stock}")
            println("Categoría: ${producto.categoria.nombre}")
        }
    }

    fun actualizarProducto() {
        listarProductos()

        val productos = productoService.obtenerProductos()
        if (productos.isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID del producto a actualizar: ")
        val producto = productoService.buscarPorId(id)

        if (producto == null) {
            println("Producto no encontrado.")
            return
        }

        println("\nProducto encontrado: ${producto.nombre}")

        val nuevoNombre = consoleUtils.leerTexto("Nuevo nombre: ")
        val nuevoPrecio = consoleUtils.leerDoublePositivo("Nuevo precio: ")
        val nuevoStock = consoleUtils.leerEnteroNoNegativo("Nuevo stock: ")

        val categorias = categoriaService.obtenerCategorias()
        println("\n--- Categorías disponibles ---")
        for (cat in categorias) {
            println("ID: ${cat.id} | Nombre: ${cat.nombre}")
        }

        val nuevaCategoriaId = consoleUtils.leerEntero("Nuevo ID de categoría: ")
        val nuevaCategoria = categoriaService.buscarPorId(nuevaCategoriaId)

        if (nuevaCategoria == null) {
            println("La categoría no existe.")
            return
        }

        producto.nombre = nuevoNombre
        producto.precio = nuevoPrecio
        producto.stock = nuevoStock
        producto.categoria = nuevaCategoria

        println("Producto actualizado correctamente.")
    }

    fun eliminarProducto() {
        listarProductos()

        val productos = productoService.obtenerProductos()
        if (productos.isEmpty()) {
            return
        }

        val id = consoleUtils.leerEntero("\nIngrese el ID del producto a eliminar: ")
        val producto = productoService.buscarPorId(id)

        if (producto == null) {
            println("Producto no encontrado.")
            return
        }

        productoService.eliminarProducto(producto)
        println("Producto eliminado correctamente.")
    }
}