package models

data class ItemListaCompra(
    val producto: Producto,
    var cantidad: Int = 1
)