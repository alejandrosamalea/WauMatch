package com.example.waumatch.data

import com.example.waumatch.ui.components.Mascota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    val uid = auth.currentUser?.uid ?: ""
    fun agregarMascota(
        mascota: Mascota,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        userId?.let { uid ->
            val docRef = db.collection("mascotas")
                .document(uid)
                .collection("lista")
                .document()

            val mascotaConId = mascota.copy(id = docRef.id)

            docRef.set(mascotaConId)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it) }

        } ?: onError(Exception("Usuario no autenticado"))
    }

    fun obtenerMascotasDelUsuario(
        onSuccess: (List<Mascota>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("mascotas")
            .document(uid)
            .collection("lista")
            .get()
            .addOnSuccessListener { result ->
                val mascotas = result.map { it.toObject(Mascota::class.java) }
                onSuccess(mascotas)
            }
            .addOnFailureListener {
                onError(it)
            }
    }
    fun actualizarMascota() {}

    fun eliminarMascota(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("mascotas").document(uid).collection("lista").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
