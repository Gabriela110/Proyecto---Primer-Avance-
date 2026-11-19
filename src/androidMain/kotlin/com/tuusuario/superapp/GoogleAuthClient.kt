
package com.tuusuario.superapp

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class GoogleAuthClient(
    private val activity: Activity
) {
    private val credentialManager = CredentialManager.create(activity)

    suspend fun iniciarSesionConGoogle(): String {

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleCredential.idToken,
                null
            )

            return suspendCancellableCoroutine { continuation ->

                FirebaseAuth.getInstance()
                    .signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid

                            if (uid != null && continuation.isActive) {
                                continuation.resume(uid)
                            } else if (continuation.isActive) {
                                continuation.resumeWithException(
                                    Exception("No se pudo obtener el UID")
                                )
                            }
                        } else if (continuation.isActive) {
                            continuation.resumeWithException(
                                task.exception
                                    ?: Exception("Error al iniciar sesión con Google")
                            )
                        }
                    }
            }
        } else {
            throw Exception("No se recibió una credencial válida de Google")
        }
    }

    companion object {
        private const val WEB_CLIENT_ID =
            "966448519452-k87e30imbte5vbjl4li8hll632kdei7u.apps.googleusercontent.com"
    }
}


