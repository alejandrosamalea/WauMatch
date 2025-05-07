package com.example.waumatch.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.waumatch.data.AnuncioRepository
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AnuncioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AnuncioRepository
    private val dao = AppDatabase.getDatabase(application).anuncioDao()

    // Flujo de anuncios mapeados a la UI
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
                    esFavorito = entity.esFavorito
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        repository = AnuncioRepository(dao)
        sincronizarDesdeFirebase()
    }

    fun agregarAnuncio(anuncio: AnuncioEntity) {
        viewModelScope.launch {
            repository.insert(anuncio)
            guardarAnuncioEnFirebase(anuncio)
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
                    creador = anuncio.creador
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
                            creador = doc.getString("creador") ?: ""
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

    private fun guardarAnuncioEnFirebase(anuncio: AnuncioEntity) {
        val auth = FirebaseAuth.getInstance()
        val userUid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(userUid).get().addOnSuccessListener { document ->
            val userName = document.getString("nombre") ?: return@addOnSuccessListener

            val anuncioMap = mapOf(
                "titulo" to anuncio.titulo,
                "descripcion" to anuncio.descripcion,
                "fechaInicio" to anuncio.fechaInicio,
                "fechaFin" to anuncio.fechaFin,
                "creador" to userName
            )

            db.collection("anuncios").add(anuncioMap)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error al obtener el nombre del usuario", exception)
        }
    }
}