package com.example.waumatch.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnuncioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anuncio: AnuncioEntity)
    @Update
    suspend fun actualizarAnuncio(anuncio: AnuncioEntity)

    @Query("SELECT * FROM anuncios ORDER BY id DESC")
    fun getAll(): Flow<List<AnuncioEntity>>

    @Query("DELETE FROM anuncios")
    suspend fun clearAll()

}