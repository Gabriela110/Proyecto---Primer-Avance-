package services

import config.FirebaseProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import utils.AuthValidation
import utils.ValidationException

class FirebaseAuthService {

    private val auth = Firebase.auth
    private val db = FirebaseProvider.db

    // Registro con correo, contraseña y rol en Firebase y Firestore
    suspend fun registrar(
        email: String,
        password: String,
        nombre: String,
        confirm: String = password,
        rol: String = "cliente"
    ): String {
        val errors = AuthValidation.validarRegistro(
            name = nombre,
            email = email,
            password = password,
            confirm = confirm
        )
        if (errors.hasErrors()) {
            throw ValidationException(errors)
        }

        val correoLimpio = email.trim()
        val nombreLimpio = nombre.trim()
        val rolLimpio = rol.trim().ifEmpty { "cliente" }

        // Crear el usuario en Authentication
        val resultado = auth.createUserWithEmailAndPassword(
            correoLimpio,
            password
        )

        val uid = resultado.user?.uid
            ?: throw Exception("No se pudo obtener el usuario registrado")

        // Crear el usuario en Firestore
        val userData = mapOf(
            "uid" to uid,
            "email" to correoLimpio,
            "nombre" to nombreLimpio,
            "rol" to rolLimpio
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
        val errors = AuthValidation.validarLogin(email, password)
        if (errors.hasErrors()) {
            throw ValidationException(errors)
        }

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
        val rol = dUsers.get<String?>("rol") ?: "cliente"
        return rol
    }
}