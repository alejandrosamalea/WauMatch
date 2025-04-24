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

    val anuncios = dao.getAll()
        .map { it }
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
            val actualizado = anuncio.copy(esFavorito = !anuncio.esFavorito)
            repository.actualizarAnuncio(actualizado)
        }
    }
    // Metodo para refrescar los anuncios desde Firebase
    fun refreshAnuncios() {
        sincronizarDesdeFirebase()
        viewModelScope.launch {
            sincronizarDesdeFirebase()
        }
    }
    private fun sincronizarDesdeFirebase() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("anuncios").get().await()

                val anunciosFirebase = snapshot.documents.mapNotNull { doc ->
                    try {
                        AnuncioEntity(
                            id = 0,
                            titulo = doc.getString("titulo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fechaInicio = doc.getString("fechaInicio") ?: "",
                            fechaFin = doc.getString("fechaFin") ?: "",
                            esFavorito = doc.getBoolean("esFavorito") ?: false,
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
        val userUid = auth.currentUser?.uid ?: return // Obtenemos el UID del usuario autenticado
        val db = FirebaseFirestore.getInstance()

        // Consulta para obtener el nombre del usuario
        db.collection("usuarios").document(userUid).get().addOnSuccessListener { document ->
            val userName = document.getString("nombre") ?: return@addOnSuccessListener // Suponiendo que el campo se llama 'nombre'

            val anuncioMap = mapOf(
                "titulo" to anuncio.titulo,
                "descripcion" to anuncio.descripcion,
                "fechaInicio" to anuncio.fechaInicio,
                "fechaFin" to anuncio.fechaFin,
                "esFavorito" to anuncio.esFavorito,
                "creador" to userName // Usamos el nombre del usuario
            )

            db.collection("anuncios").add(anuncioMap)
        }.addOnFailureListener { exception ->
            Log.e("Firebase", "Error al obtener el nombre del usuario", exception)
        }
    }

}
