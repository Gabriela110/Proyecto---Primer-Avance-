
package services

import config.FirebaseProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

class FirebaseAuthService {

    private val auth = Firebase.auth
    private val db = FirebaseProvider.db

    // Registro con correo, contraseña y rol en Firebase y Firestore
    suspend fun registrar(
        email: String,
        password: String,
        nombre: String,
        rol: String // "admin" o "client"
    ): String {
        // Crear el usuario en Authentication
        val resultado = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        )

        val uid = resultado.user?.uid
            ?: throw Exception("No se pudo obtener el usuario registrado")

        // Crear el usuario en Firestore
        val userData = mapOf(
            "uid" to uid,
            "email" to email.trim(),
            "nombre" to nombre,
            "rol" to "cliente"
        )

        try {
            db.collection("users").document(uid).set(userData)
        } catch (e: Exception) {
            throw Exception("Usuario creado en Auth, pero falló al guardar el perfil: ${e.message}")
        }

        return uid
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

    suspend fun obtenerRolUsuario(uid: String): String {
        val dUsers = db.collection("users").document(uid).get()
        val rol = dUsers.get<String>("rol") ?: "cliente"
        return rol
    }
}
