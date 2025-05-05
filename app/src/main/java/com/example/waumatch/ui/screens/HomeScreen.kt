package com.example.waumatch.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.waumatch.ui.components.AnuncioCard
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waumatch.ui.components.WauMatchHeader
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory


@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncios by viewModel.anuncios.collectAsState()
    var anuncioExpandido by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            WauMatchHeader()
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(anuncios) { anuncio ->
                    AnuncioCard(
                        anuncio = anuncio,
                        isExpanded = anuncioExpandido == anuncio.id,
                        onClick = { anuncioExpandido = anuncio.id },
                        onClose = { anuncioExpandido = null },
                        onToggleFavorito = { viewModel.toggleFavorito(it) })
                }
            }
        }
    }
}
