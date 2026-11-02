
package services

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class FirebaseAuthService {

    private val auth = Firebase.auth

    // Registro con correo y contraseña
    suspend fun registrar(
        email: String,
        password: String
    ): String {

        val resultado = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        )

        return resultado.user?.uid
            ?: throw Exception("No se pudo obtener el usuario registrado")
    }

    // Inicio de sesión
    suspend fun iniciarSesion(
        email: String,
        password: String
    ): String {

        val resultado = auth.signInWithEmailAndPassword(
            email.trim(),
            password
        )

        return resultado.user?.uid
            ?: throw Exception("No se pudo iniciar sesión")
    }

    // Cerrar sesión
    suspend fun cerrarSesion() {
        auth.signOut()
    }

    // Obtener UID del usuario autenticado
    fun obtenerUidActual(): String? {
        return auth.currentUser?.uid
    }

    // Obtener correo del usuario autenticado
    fun obtenerCorreoActual(): String? {
        return auth.currentUser?.email
    }

    // Comprobar sesión activa
    fun haySesionActiva(): Boolean {
        return auth.currentUser != null
    }
}
