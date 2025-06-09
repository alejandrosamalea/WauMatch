package com.example.waumatch.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.waumatch.data.AnuncioRepository
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.data.local.AppDatabase
import com.example.waumatch.ui.components.Mascota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AnuncioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AnuncioRepository
    private val dao = AppDatabase.getDatabase(application).anuncioDao()

    var comunidadUsuarioActual: String? = null
        private set

    val anuncios = dao.getAll()
        .map { entities ->
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""
            entities.map { entity ->
                AnuncioEntity(
                    id = entity.id,
                    titulo = entity.titulo,
                    descripcion = entity.descripcion,
                    fechaInicio = entity.fechaInicio,
                    fechaFin = entity.fechaFin,
                    creador = entity.creador,
                    idCreador = entity.idCreador,
                    esFavorito = entity.esFavorito,
                    imagenes = entity.imagenes,
                    tipos = entity.tipos,
                    mascotasIds = entity.mascotasIds,
                    latitud =  entity.latitud,
                    longitud =  entity.longitud
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        repository = AnuncioRepository(dao)
        sincronizarDesdeFirebase()
        cargarComunidadUsuarioActual()
    }

    fun cargarComunidadUsuarioActual() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                comunidadUsuarioActual = document.getString("provincia") // o "comunidad"
            }
            .addOnFailureListener {
                Log.e("AnuncioViewModel", "Error al obtener la comunidad del usuario actual", it)
            }
    }

    suspend fun perteneceALaComunidadDelUsuario(idCreador: String): Boolean {
        val comunidadUsuario = comunidadUsuarioActual ?: return false

        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(idCreador)
                .get()
                .await()
            val comunidadCreador = snapshot.getString("provincia")
            comunidadCreador == comunidadUsuario
        } catch (e: Exception) {
            false
        }
    }


    fun agregarAnuncio(anuncio: AnuncioEntity, context: Context) {
        viewModelScope.launch {
            repository.insert(anuncio)
            guardarAnuncioEnFirebase(anuncio, context)
        }
    }

    fun toggleFavorito(anuncio: AnuncioEntity) {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser ?: return@launch
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("usuarios").document(user.uid)

            try {
                val snapshot = userDocRef.get().await()
                val matchIds = snapshot.get("matchIds") as? List<String> ?: emptyList()
                val anuncioId = anuncio.id

                val nuevosMatchIds = if (matchIds.contains(anuncioId)) {
                    matchIds - anuncioId
                } else {
                    matchIds + anuncioId
                }

                userDocRef.update("matchIds", nuevosMatchIds).await()

                val actualizado = AnuncioEntity(
                    id = anuncio.id,
                    titulo = anuncio.titulo,
                    descripcion = anuncio.descripcion,
                    fechaInicio = anuncio.fechaInicio,
                    fechaFin = anuncio.fechaFin,
                    esFavorito = nuevosMatchIds.contains(anuncioId),
                    creador = anuncio.creador,
                    idCreador = anuncio.idCreador,
                    imagenes = anuncio.imagenes,
                    tipos = anuncio.tipos,
                    mascotasIds = anuncio.mascotasIds,
                    latitud =  anuncio.latitud,
                    longitud =  anuncio.longitud
                )
                repository.actualizarAnuncio(actualizado)

            } catch (e: Exception) {
                Log.e("toggleFavorito", "Error al actualizar favoritos: ${e.message}")
            }
        }
    }

    fun refreshAnuncios() {
        sincronizarDesdeFirebase()
    }

    private fun sincronizarDesdeFirebase() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val user = auth.currentUser

                val matchIds: List<String> = if (user != null) {
                    try {
                        val userSnapshot = db.collection("usuarios").document(user.uid).get().await()
                        userSnapshot.get("matchIds") as? List<String> ?: emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                val snapshot = db.collection("anuncios").get().await()

                val anunciosFirebase = snapshot.documents.mapNotNull { doc ->
                    try {
                        val anuncioId = doc.id
                        AnuncioEntity(
                            id = anuncioId,
                            titulo = doc.getString("titulo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fechaInicio = doc.getString("fechaInicio") ?: "",
                            fechaFin = doc.getString("fechaFin") ?: "",
                            esFavorito = matchIds.contains(anuncioId),
                            creador = doc.getString("creador") ?: "",
                            idCreador = doc.getString("idCreador") ?: "",
                            imagenes = doc.get("imagenes") as? List<String> ?: listOf(),
                            tipos = doc.getString("tipos") ?: "",
                            mascotasIds = doc.get("mascotasIds") as? List<String> ?: listOf(),
                            latitud = doc.getDouble("latitud") ?: 0.0,
                            longitud = doc.getDouble("longitud") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                dao.clearAll()
                anunciosFirebase.forEach { dao.insert(it) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun guardarAnuncioEnFirebase(anuncio: AnuncioEntity, context: Context) {
        val auth = FirebaseAuth.getInstance()
        val userUid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(userUid).get().addOnSuccessListener { document ->
            val userName = document.getString("nombre") ?: return@addOnSuccessListener

            val uploadedImageUrls = mutableListOf<String>()

            anuncio.imagenes.filterNotNull().forEachIndexed { index, imageUri ->
                val uri = Uri.parse(imageUri)
                val options = mapOf("public_id" to "anuncio_images/${anuncio.id}_$index")

                MediaManager.get().upload(uri)
                    .options(options)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Toast.makeText(context, "Subiendo imagen ${index + 1}...", Toast.LENGTH_SHORT).show()
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val url = resultData["secure_url"] as String
                            uploadedImageUrls.add(url)

                            if (uploadedImageUrls.size == anuncio.imagenes.size) {
                                val anuncioMap = mapOf(
                                    "titulo" to anuncio.titulo,
                                    "descripcion" to anuncio.descripcion,
                                    "fechaInicio" to anuncio.fechaInicio,
                                    "fechaFin" to anuncio.fechaFin,
                                    "creador" to userName,
                                    "idCreador" to anuncio.idCreador,
                                    "imagenes" to uploadedImageUrls,
                                    "tipos" to anuncio.tipos,
                                    "mascotasIds" to anuncio.mascotasIds,
                                    "latitud" to anuncio.latitud,
                                    "longitud" to anuncio.longitud
                                )

                                db.collection("anuncios").add(anuncioMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Anuncio guardado con imágenes", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error al guardar anuncio: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }

                        override fun onError(requestId: String, @SuppressLint("RestrictedApi") error: ErrorInfo) {}
                        override fun onReschedule(requestId: String, @SuppressLint("RestrictedApi") error: ErrorInfo) {}
                    })
                    .dispatch()
            }
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error al obtener el nombre del usuario", exception)
        }
    }

    fun getAnuncioById(id: String): Flow<AnuncioEntity?> {
        return anuncios.map { list -> list.find { it.id == id } }
    }
    fun obtenerIdUsuarioActual(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // Función para calcular la distancia usando la fórmula de Haversine
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en kilómetros
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c // Distancia en kilómetros
    }

    private fun toRadians(degrees: Double): Double = degrees * PI / 180.0

    fun getMascotasByIds(userId: String, mascotaIds: List<String>, onResult: (List<Mascota>) -> Unit) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val mascotas = mutableListOf<Mascota>()

            try {
                val snapshot = db.collection("mascotas")
                    .document(userId)
                    .collection("lista")
                    .get()
                    .await()

                for (doc in snapshot.documents) {
                    if (doc.id in mascotaIds) {
                        doc.toObject(Mascota::class.java)?.let { mascota ->
                            mascotas.add(mascota.copy(id = doc.id))
                        }
                    }
                }
                onResult(mascotas)
            } catch (e: Exception) {
                Log.e("AnuncioScreen", "Error al obtener mascotas: ${e.message}")
                onResult(emptyList())
            }
        }
    }
    fun eliminarAnuncio(anuncioId: String, context: Context) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            try {
                // 1. Eliminar de Firebase
                db.collection("anuncios").document(anuncioId).delete().await()

                // 2. Eliminar de Room (si existe)
                repository.eliminarPorId(anuncioId)

                // 3. Mostrar mensaje de éxito
                Toast.makeText(context, "Anuncio eliminado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("eliminarAnuncio", "Error al eliminar anuncio: ${e.message}")
                Toast.makeText(context, "Error al eliminar el anuncio", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
