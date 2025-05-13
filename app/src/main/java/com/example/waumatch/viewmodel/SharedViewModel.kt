package com.example.waumatch.viewmodel

import androidx.lifecycle.ViewModel
import com.example.waumatch.data.local.AnuncioEntity

class SharedAnuncioViewModel : ViewModel() {
    var anuncioSeleccionado: AnuncioEntity? = null
}
