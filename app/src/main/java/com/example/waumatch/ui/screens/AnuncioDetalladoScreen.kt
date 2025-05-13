package com.example.waumatch.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.log

@Composable
fun AnuncioDetalladoScreen(
    navController: NavController,
    anuncioId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    val anuncio = viewModel.getAnuncioById(anuncioId).collectAsState(initial = null).value
    val auth = FirebaseAuth.getInstance()

    if (anuncio == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF024873), Color(0xFF1D7A93))
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            top = 16.dp,
                            start = 16.dp
                        ) // Ajuste: era `end =`, ahora es `start =`
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
                Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen
                Image(
                    painter = rememberAsyncImagePainter(model = anuncio.imagenes.firstOrNull()),
                    contentDescription = "Imagen del anuncio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 250.dp)
                        .padding(bottom = 16.dp),
                )

                // Creador subrayado
                Text(
                    text = "Creador: ${anuncio.creador}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .align(Alignment.Start)
                        .clickable {
                            Log.d("NAVEGACION", "Navegando a perfil de: ${anuncio.idCreador}")
                            navController.navigate("foreignProfile/${anuncio.idCreador}")
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.White,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )


                // Título
                Text(
                    text = anuncio.titulo,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )

                // Descripción
                Text(
                    text = anuncio.descripcion,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )

                // Fechas de disponibilidad
                Text(
                    text = "Disponibilidad:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "Desde: ${anuncio.fechaInicio}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.Start)
                )
                Text(
                    text = "Hasta: ${anuncio.fechaFin}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 32.dp)
                )

                // Botón centrado
                Button(
                    onClick = { /* Acción del botón */ },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = "Contactar", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

