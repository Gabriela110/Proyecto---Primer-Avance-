package config

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.firestore.firestore

object FirebaseProvider {
    val db = Firebase.firestore(Firebase.app("[DEFAULT]"), "superappdb")
}