package com.example.waumatch.data

import android.util.Log
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
    fun actualizarMascota(mascota: Mascota, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val mascotaId = mascota.id
        if (mascotaId.isNullOrEmpty()) {
            onFailure(Exception("Id de mascota null o vacío"))
            return
        }

        val mascotaMap = mapOf(
            "nombre" to mascota.nombre,
            "especie" to mascota.especie,
            "raza" to mascota.raza,
            "edad" to mascota.edad,
            "imagenes" to mascota.imagenes,

            "horariosComida" to mascota.horariosComida,
            "tipoComida" to mascota.tipoComida,
            "ritualesComida" to mascota.ritualesComida,

            "lugarDormir" to mascota.lugarDormir,
            "horarioDormir" to mascota.horarioDormir,

            "habitosHigiene" to mascota.habitosHigiene,
            "frecuenciaPaseo" to mascota.frecuenciaPaseo,

            "limpieza" to mascota.limpieza,
            "productosEspeciales" to mascota.productosEspeciales,

            "juguetesFavoritos" to mascota.juguetesFavoritos,

            "medicacion" to mascota.medicacion,
            "veterinario" to mascota.veterinario,

            "restricciones" to mascota.restricciones,
            "accionesNoToleradas" to mascota.accionesNoToleradas,

            "puedeQuedarseSolo" to mascota.puedeQuedarseSolo,
            "ansiedadSeparacion" to mascota.ansiedadSeparacion,

            "escapa" to mascota.escapa,
            "habitacionesRestringidas" to mascota.habitacionesRestringidas,

            "adicional" to mascota.adicional,

            "idDuenio" to mascota.idDuenio,
            "telefonoDuenio" to mascota.telefonoDuenio,
            "correoDuenio" to mascota.correoDuenio,
            "contactoAlternativo" to mascota.contactoAlternativo
        )
        Log.i("ADMIN","ns que pollas pasa: ${mascotaId}")
        db.collection("mascotas").document(uid).collection("lista").document(mascota.id)
            .update(mascotaMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.i("ADMIN", "Error al actualizar campos")
                onFailure(e)
            }
    }



    fun eliminarMascota(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("mascotas").document(uid).collection("lista").document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    fun obtenerMascotaPorId(
        mascotaId: String,
        onSuccess: (Mascota) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("mascotas")
            .document(uid)
            .collection("lista")
            .document(mascotaId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val mascota = document.toObject(Mascota::class.java)
                    if (mascota != null) {
                        onSuccess(mascota)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}
