package com.example.waumatch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.ui.components.Mascota
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MascotaViewModel : ViewModel() {
    private val _mascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val mascotas: StateFlow<List<Mascota>> = _mascotas.asStateFlow()

    fun cargarMascotasPorIdBuscado(idBuscado: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val mascotasList = mutableListOf<Mascota>()

            try {
                val snapshot = db.collectionGroup("lista")
                    .whereEqualTo("id", idBuscado)
                    .get()
                    .await()

                for (document in snapshot.documents) {
                    val mascota = document.toObject(Mascota::class.java)
                    if (mascota != null) {
                        mascotasList.add(mascota)
                    }
                }

                _mascotas.value = mascotasList

            } catch (e: Exception) {
                Log.e("MascotaViewModel", "Error al cargar mascotas con id = $idBuscado", e)
            }
        }
    }

}