package com.example.waumatch.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.components.WauMatchHeader
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun FavoritesScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncios by viewModel.anuncios.collectAsState()
    var favoritos by remember { mutableStateOf<List<String>>(emptyList()) }

    // Cargar los matchIds del usuario
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userSnapshot = db.collection("usuarios").document(user.uid).get().await()
            favoritos = userSnapshot.get("matchIds") as? List<String> ?: emptyList()
        }
    }

    val anunciosFavoritos = anuncios.filter { favoritos.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WauMatchHeader()
        Spacer(modifier = Modifier.height(16.dp))

        if (anunciosFavoritos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes anuncios en favoritos.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(anunciosFavoritos) { anuncio ->
                    AnuncioCard(
                        anuncio = anuncio,
                        isExpanded = false,
                        onClick = {},
                        onClose = {},
                        onToggleFavorito = { viewModel.toggleFavorito(it) }
                    )
                }
            }
        }
    }
}
