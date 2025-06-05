package com.example.waumatch.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anuncios")
data class AnuncioEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val esFavorito: Boolean = false,
    val creador: String,
    val idCreador: String,
    val imagenes: List<String>,
    val tipos: String,
    val mascotasIds: List<String>,
    val latitud: Double,
    val longitud: Double
)

