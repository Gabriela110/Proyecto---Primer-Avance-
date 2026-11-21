package models

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "cliente"
)