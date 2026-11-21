package models

import kotlinx.serialization.Serializable

@Serializable
data class ItemCarrito(
    val userId: String = "",
    val productoId: String = "",
    val cantidad: Int = 0
)