package com.example.waumatch.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.ColumnScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.waumatch.data.AnuncioRepository
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnuncioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AnuncioRepository
    private val dao = AppDatabase.getDatabase(application).anuncioDao()
    //    // Flujo de anuncios mapeados a la UI
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
                    imagenes = entity.imagenes
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        repository = AnuncioRepository(dao)
        sincronizarDesdeFirebase()
    }

    fun agregarAnuncio(anuncio: AnuncioEntity, context: Context) {
        viewModelScope.launch {
            repository.insert(anuncio)
            guardarAnuncioEnFirebase(anuncio,context)
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
                    matchIds - anuncioId // Quitar de favoritos
                } else {
                    matchIds + anuncioId // Agregar a favoritos
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
                    imagenes = anuncio.imagenes
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

                            imagenes = doc.get("imagenes") as? List<String> ?: listOf()
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
                                    "imagenes" to uploadedImageUrls
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

                        override fun onError(requestId: String, @SuppressLint("RestrictedApi") error: ErrorInfo) {
                        }

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

}