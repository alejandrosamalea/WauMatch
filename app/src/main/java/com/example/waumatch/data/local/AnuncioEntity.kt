package com.example.waumatch.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anuncios")
data class AnuncioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val esFavorito: Boolean = false,
    val creador: String
)
