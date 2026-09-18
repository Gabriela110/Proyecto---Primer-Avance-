package models

abstract class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    private val contrasena: String
) {

    abstract val rol: String

    fun validarContrasena(contrasenaIngresada: String): Boolean {
        return contrasena == contrasenaIngresada
    }

    abstract fun mostrarMenu()

    override fun toString(): String {
        return "ID: $id | Nombre: $nombre | Correo: $correo | Rol: $rol"
    }
}