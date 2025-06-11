package com.example.waumatch.ui.screens

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForeignAdsScreen(
    userId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncios by viewModel.anuncios.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val foreignAnuncios = anuncios
        .filter { it.idCreador == userId }
        .sortedBy { anuncio ->
            // Convierte el string a LocalDate para ordenar cronológicamente
            LocalDate.parse(anuncio.fechaFin, formatter)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anuncios del Usuario",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OceanBlue,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(colors = listOf(OceanBlue, SkyBlue))
        )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (foreignAnuncios.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Este usuario no tiene anuncios.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(foreignAnuncios) { anuncio ->
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