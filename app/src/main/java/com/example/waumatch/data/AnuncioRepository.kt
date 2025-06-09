package com.example.waumatch.data

import com.example.waumatch.data.local.AnuncioDao
import com.example.waumatch.data.local.AnuncioEntity
import kotlinx.coroutines.flow.Flow

class AnuncioRepository(private val dao: AnuncioDao) {
    suspend fun insert(anuncio: AnuncioEntity) = dao.insert(anuncio)
    suspend fun actualizarAnuncio(anuncio: AnuncioEntity) {
        dao.actualizarAnuncio(anuncio)
    }
    suspend fun eliminarPorId(id: String) {
        dao.deleteById(id)
    }

    fun getAll(): Flow<List<AnuncioEntity>> = dao.getAll()
}