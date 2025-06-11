package com.example.waumatch.ui.screens

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.components.SearchBar
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncios by viewModel.anuncios.collectAsState()
    var favoritos by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val userSnapshot = db.collection("usuarios").document(user.uid).get().await()
            favoritos = userSnapshot.get("matchIds") as? List<String> ?: emptyList()
        }
    }

    // Filtrar anuncios favoritos
    val anunciosFavoritos = anuncios.filter { favoritos.contains(it.id) }
    // Filtrar por búsqueda (buscar en título o descripción)
    val anunciosFiltrados = if (searchQuery.isBlank()) {
        anunciosFavoritos
    } else {
        anunciosFavoritos.filter {
            it.titulo.contains(searchQuery, ignoreCase = true) ||
                    it.descripcion.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF024873), Color(0xFF1D7A93))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra de búsqueda
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onFilterClick = { /* Lógica de filtro */ }
            )

            // Cuadrícula de anuncios favoritos o mensaje si está vacío
            if (anunciosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes anuncios en favoritos o no coinciden con la búsqueda.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(anunciosFiltrados) { anuncio ->
                        AnuncioCard(
                            anuncio = anuncio,
                            onClick = { navController.navigate("anuncioDetallado/${anuncio.id}") },
                            onToggleFavorito = { viewModel.toggleFavorito(it) }
                        )
                    }
                }
            }
        }
    }
}