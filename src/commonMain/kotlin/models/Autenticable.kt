package models

interface Autenticable {

    fun iniciarSesion(): Boolean

    fun cerrarSesion()
}