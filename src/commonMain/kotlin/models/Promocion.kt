package models

data class Promocion(
    val id: String,
    var nombre: String,
    val productoId: String,
    val descuento: Double,
    val activa: Boolean = true
)