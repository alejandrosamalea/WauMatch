package com.example.waumatch.viewmodel

import com.google.firebase.auth.FirebaseAuth

data class UserApp(
    val id: String,        // UID de Firebase
    val name: String       // Nombre mostrado en la app
)
fun getCurrentUserApp(): UserApp? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    return if (uid != null) {
        UserApp(id = uid, name = "Yo") // o trae el nombre desde Firestore si lo guardas
    } else null
}