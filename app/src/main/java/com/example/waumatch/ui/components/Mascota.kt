package com.example.waumatch.ui.components

data class Mascota(
    val id: String = "",
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val edad: Int = 0,

    // Fotos
    val imagenes: List<String> = emptyList(),

    // Rutinas
    val horariosComida: String = "",
    val tipoComida: String = "",
    val ritualesComida: String = "",

    // Descanso
    val lugarDormir: String = "",
    val horarioDormir: String = "",

    // Higiene
    val habitosHigiene: String = "",
    val frecuenciaPaseo: String = "",

    // Limpieza
    val limpieza: String = "",
    val productosEspeciales: String = "",

    // Juguetes
    val juguetesFavoritos: String = "",

    // Salud
    val medicacion: String = "",
    val veterinario: String = "",

    // Restricciones
    val restricciones: String = "",
    val accionesNoToleradas: String = "",

    // Ausencias
    val puedeQuedarseSolo: String = "",
    val ansiedadSeparacion: String = "",

    // Seguridad
    val escapa: String = "",
    val habitacionesRestringidas: String = "",

    // ADicional
    val adicional: String = "",

    // Dueño
    val idDuenio: String = "",
    val telefonoDuenio: String = "",
    val correoDuenio: String = "",
    val contactoAlternativo: String = ""
)
