package models

data class Promocion(
    val id: Int,
    var nombre: String,
    var descuento: Double,
    var productoId: Int
)