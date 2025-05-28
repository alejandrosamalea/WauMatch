package com.example.waumatch.ui.screens

import android.app.Application
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.DefaultTab.AlbumsTab.value
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.components.SearchBar
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncios by viewModel.anuncios.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filtrarPorComunidad by remember { mutableStateOf(false) }

    val userLocation by produceState<List<Double>?>(initialValue = null) {
        val idUsuario = viewModel.obtenerIdUsuarioActual() ?: ""
        try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("usuarios").document(idUsuario).get().await()
            if (doc.exists()) {
                val lat = doc.getDouble("latitud")
                val lng = doc.getDouble("longitud")
                val rad = doc.getDouble("radio_km")
                if (lat != null && lng != null && rad != null) {
                    value = listOf(lat, lng, rad)
                }
            }
        } catch (e: Exception) {
            value = null
        }
    }

    val anunciosFiltrados by produceState(
        initialValue = emptyList<AnuncioEntity>(),
        userLocation,
        searchQuery,
        filtrarPorComunidad,
        anuncios
    ) {
        if (userLocation == null) {
            value = emptyList()
            return@produceState
        }

        val idUsuario = viewModel.obtenerIdUsuarioActual() ?: ""

        val lista = anuncios.filter { anuncio ->
            val noEsDelUsuario = anuncio.idCreador != idUsuario
            val coincideBusqueda = searchQuery.isBlank() ||
                    anuncio.titulo.contains(searchQuery, true) ||
                    anuncio.descripcion.contains(searchQuery, true)
            val coincideComunidad = !filtrarPorComunidad ||
                    viewModel.perteneceALaComunidadDelUsuario(anuncio.idCreador)


            val distancia = viewModel.calculateDistance(
                lat1 = userLocation!![0],
                lon1 = userLocation!![1],
                lat2 = anuncio.latitud,
                lon2 = anuncio.longitud
            )

            val dentroDelRadio = distancia <= userLocation!![2]


            noEsDelUsuario && coincideBusqueda && coincideComunidad && dentroDelRadio
        }

        value = lista
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
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onFilterClick = { /* Lógica de filtro */ }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Checkbox(
                    checked = filtrarPorComunidad,
                    onCheckedChange = { filtrarPorComunidad = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color.White, uncheckedColor = Color.White)
                )
                Text(
                    text = "Filtrar por tu comunidad",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }

            if (anunciosFiltrados.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (anuncios.isEmpty()) "No hay anuncios disponibles."
                        else "No se encontraron anuncios que coincidan con los filtros.",
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
