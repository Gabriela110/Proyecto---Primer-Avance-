package models

data class Producto(
    val id: String = "",
    val nombre: String,
    val precio: Double,
    val categoriaId: String = "",
    var categoria: Categoria = Categoria(id = categoriaId, nombre = ""),
    val stock: Int = 0,
    val imagenUrl: String = ""
)