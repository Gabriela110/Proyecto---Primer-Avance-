package models

data class Promocion(
    val id: String = "",
    val nombre: String = "",
    val productoId: String,
    val porcentajeDescuento: Double = 0.0,
    val descuento: Double = porcentajeDescuento,
    val descripcion: String = "",
    val activa: Boolean = true
)