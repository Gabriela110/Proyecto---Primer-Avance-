val productos = mutableListOf<Producto>()
val promociones = mutableListOf<Promocion>()
val categorias = mutableListOf<Categoria>()

var siguienteProductoId = 1
var siguientePromocionId= 1
var siguienteCategoriaId = 1

fun main() {
    cargarCategoriasIniciales()

    var opcion: Int

    do {
        println("\n========== SISTEMA DE PRODUCTOS Y PROMOCIONES ==========")
        println("1. Gestionar productos")
        println("2. Gestionar promociones")
        println("3. Gestionar categorías")
        println("4. Salir")
        println("==========================================================")

        opcion = leerEntero("Seleccione una opción: ")

        when (opcion) {
            1 -> menuProductos()
            2 -> menuPromociones()
            3 -> menuCategorias()
            4 -> println("Saliendo del sistema...")
            else -> println("Opción inválida.")
        }

    }while (opcion != 4)
}
// ==========================================================
// PRODUCTOS
// ==========================================================

fun menuProductos() {

     var opcion: Int
     do {
        println("\n========== PRODUCTOS ==========")
        println("1. Crear producto")
        println("2. Listar productos")
        println("3. Actualizar producto")
        println("4. Eliminar producto")
        println("5. Regresar")

        opcion = leerEntero("Seleccione una opción: ")

        when (opcion) {
            1 -> crearProducto()
            2 -> listarProductos()
            3 -> actualizarProducto()
            4 -> eliminarProducto()
            5 -> println("Regresando...")
            else -> println("Opción inválida.")
     }
     
     }while (opcion != 5)
}

fun crearProducto() {

    println("\n========== CREAR PRODUCTO ==========")
    val nombre = leerTexto("Nombre del producto: ")
    val precio = leerDoublePositivo("Precio: ")
    val stock = leerEnteroNoNegativo("Stock: ")

    if (categorias.isEmpty()) {
        println("No existen categorías. Cree una categoría primero.")
        return
    }

    listarCategorias()

    val categoriaId = leerEntero("Ingrese el ID de la categoría: ")

    val categoria = categorias.find { it.id == categoriaId }

    if (categoria == null) {
        println("La categoría no existe.")
        return
    }

    val producto = Producto(
        siguienteProductoId,
        nombre,
        precio,
        stock,
        categoria
    )

    productos.add(producto)
    siguienteProductoId++

    println("Producto creado correctamente.")
}


fun listarProductos() {

    println("\n========== LISTA DE PRODUCTOS ==========")

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

    if (productos.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID del producto a actualizar: ")

    val producto = productos.find { it.id == id }

    if (producto == null) {
        println("Producto no encontrado.")
        return
    }

    println("\nProducto encontrado: ${producto.nombre}")

    val nuevoNombre = leerTexto("Nuevo nombre: ")
    val nuevoPrecio = leerDoublePositivo("Nuevo precio: ")
    val nuevoStock = leerEnteroNoNegativo("Nuevo stock: ")

    listarCategorias()

    val nuevaCategoriaId = leerEntero("Nuevo ID de categoría: ")

    val nuevaCategoria = categorias.find { it.id == nuevaCategoriaId }

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

    if (productos.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID del producto a eliminar: ")

    val producto = productos.find { it.id == id }

    if (producto == null) {
        println("Producto no encontrado.")
        return
    }

    productos.remove(producto)

    promociones.removeIf { it.productoId == id }

    println("Producto eliminado correctamente.")
}
// ==========================================================
// PROMOCIONES
// ==========================================================

fun menuPromociones() {

    var opcion: Int

    do {
        println("\n========== PROMOCIONES ==========")
        println("1. Crear promoción")
        println("2. Listar promociones")
        println("3. Actualizar promoción")
        println("4. Eliminar promoción")
        println("5. Regresar")

        opcion = leerEntero("Seleccione una opción: ")

        when (opcion) {
            1 -> crearPromocion()
            2 -> listarPromociones()
            3 -> actualizarPromocion()
            4 -> eliminarPromocion()
            5 -> println("Regresando...")
            else -> println("Opción inválida.")
        }

    } while (opcion != 5)
}


fun crearPromocion() {

    println("\n========== CREAR PROMOCIÓN ==========")

    if (productos.isEmpty()) {
        println("Debe crear al menos un producto primero.")
        return
    }

    val nombre = leerTexto("Nombre de la promoción: ")

    val descuento = leerDescuento()

    listarProductos()

    val productoId = leerEntero("\nID del producto: ")

    val producto = productos.find { it.id == productoId }

    if (producto == null) {
        println("El producto no existe.")
        return
    }

    val promocion = Promocion(
        siguientePromocionId,
        nombre,
        descuento,
        productoId
    )

    promociones.add(promocion)
    siguientePromocionId++

    println("Promoción creada correctamente.")
}


fun listarPromociones() {

    println("\n========== LISTA DE PROMOCIONES ==========")

    if (promociones.isEmpty()) {
        println("No hay promociones registradas.")
        return
    }

    for (promocion in promociones) {

        val producto = productos.find {
            it.id == promocion.productoId
        }

        println("----------------------------------------")
        println("ID: ${promocion.id}")
        println("Nombre: ${promocion.nombre}")
        println("Descuento: ${promocion.descuento}%")
        println("Producto: ${producto?.nombre ?: "Producto eliminado"}")
    }
}


fun actualizarPromocion() {

    listarPromociones()

    if (promociones.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID de la promoción: ")

    val promocion = promociones.find { it.id == id }

    if (promocion == null) {
        println("Promoción no encontrada.")
        return
    }

    val nuevoNombre = leerTexto("Nuevo nombre: ")
    val nuevoDescuento = leerDescuento()

    promocion.nombre = nuevoNombre
    promocion.descuento = nuevoDescuento

    println("Promoción actualizada correctamente.")
}


fun eliminarPromocion() {

    listarPromociones()

    if (promociones.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID de la promoción: ")

    val promocion = promociones.find { it.id == id }

    if (promocion == null) {
        println("Promoción no encontrada.")
        return
    }

    promociones.remove(promocion)

    println("Promoción eliminada correctamente.")
}
// ==========================================================
// CATEGORÍAS
// ==========================================================

fun menuCategorias() {

    var opcion: Int

    do {
        println("\n========== CATEGORÍAS ==========")
        println("1. Crear categoría")
        println("2. Listar categorías")
        println("3. Actualizar categoría")
        println("4. Eliminar categoría")
        println("5. Regresar")

        opcion = leerEntero("Seleccione una opción: ")

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

    val nombre = leerTexto("Nombre de la categoría: ")

    val categoria = Categoria(
        siguienteCategoriaId,
        nombre
    )

    categorias.add(categoria)
    siguienteCategoriaId++

    println("Categoría creada correctamente.")
}
fun listarCategorias() {

    println("\n========== LISTA DE CATEGORÍAS ==========")

    if (categorias.isEmpty()) {
        println("No hay categorías registradas.")
        return
    }

    for (categoria in categorias) {
        println("----------------------------------------")
        println("ID: ${categoria.id}")
        println("Nombre: ${categoria.nombre}")
    }
}
fun actualizarCategoria() {

    listarCategorias()

    if (categorias.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID de la categoría: ")

    val categoria = categorias.find { it.id == id }

    if (categoria == null) {
        println("Categoría no encontrada.")
        return
    }

    val nuevoNombre = leerTexto("Nuevo nombre: ")

    categoria.nombre = nuevoNombre

    println("Categoría actualizada correctamente.")
}
fun eliminarCategoria() {

    listarCategorias()

    if (categorias.isEmpty()) {
        return
    }

    val id = leerEntero("\nIngrese el ID de la categoría: ")

    val categoria = categorias.find { it.id == id }

    if (categoria == null) {
        println("Categoría no encontrada.")
        return
    }

    val productosUsandoCategoria = productos.any {
        it.categoria.id == id
    }

    if (productosUsandoCategoria) {
        println("No se puede eliminar la categoría porque tiene productos asociados.")
        return
    }

    categorias.remove(categoria)

    println("Categoría eliminada correctamente.")
}
// ==========================================================
// CARGAR CATEGORÍAS INICIALES
// ==========================================================

fun cargarCategoriasIniciales() {

    categorias.add(Categoria(siguienteCategoriaId, "Alimentos"))
    siguienteCategoriaId++

    categorias.add(Categoria(siguienteCategoriaId, "Bebidas"))
    siguienteCategoriaId++

    categorias.add(Categoria(siguienteCategoriaId, "Limpieza"))
    siguienteCategoriaId++

    categorias.add(Categoria(siguienteCategoriaId, "Higiene personal"))
    siguienteCategoriaId++
}
// ==========================================================
// VALIDACIONES
// ==========================================================

fun leerTexto(mensaje: String): String {

    while (true) {

        print(mensaje)

        val texto = readLine()?.trim() ?: ""

        if (texto.isNotEmpty()) {
            return texto
        }

        println("El texto no puede estar vacío.")
    }
}
fun leerDoublePositivo(mensaje: String): Double {

    while (true) {

        print(mensaje)

        val numero = readLine()?.toDoubleOrNull()

        if (numero != null && numero > 0) {
            return numero
        }

        println("Ingrese un número mayor que 0.")
    }
}
fun leerEnteroNoNegativo(mensaje: String): Int {

    while (true) {

        print(mensaje)

        val numero = readLine()?.toIntOrNull()

        if (numero != null && numero >= 0) {
            return numero
        }

        println("Ingrese un número entero mayor o igual a 0.")
    }
}
fun leerDescuento(): Double {

    while (true) {

        print("Descuento (%): ")

        val descuento = readLine()?.toDoubleOrNull()

        if (descuento != null && descuento > 0 && descuento <= 100) {
            return descuento
        }

        println("El descuento debe estar entre 0 y 100.")
    }
}