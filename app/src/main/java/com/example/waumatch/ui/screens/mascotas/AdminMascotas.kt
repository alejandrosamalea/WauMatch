package com.example.waumatch.ui.screens.mascotas

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.data.MascotaRepository
import com.example.waumatch.ui.components.Mascota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMascota(
    navController: NavController,
    repository: MascotaRepository = MascotaRepository()
) {
    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.obtenerMascotasDelUsuario(
            onSuccess = {
                mascotas = it
                Log.i("ADMIN", "ENTRO MI LOCO")
                Log.i("ADMIN", it.size.toString())

            },
            onError = { Log.e("ManagePets", "Error", it) }
        )
    }

    Column {
        TopAppBar(
            title = { Text("Mis Mascotas") },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        if (mascotas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Actualmente no tienes mascotas registradas")
            }
        } else {
            LazyColumn {
                items(mascotas) { mascota ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(model = mascota.imagenes.firstOrNull()),
                                contentDescription = "Imagen del anuncio",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text("Nombre: ${mascota.nombre}")
                            Text("Especie: ${mascota.especie}")
                            Text("Raza: ${mascota.raza}")
                            Text("Edad: ${mascota.edad}")
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    repository.eliminarMascota(mascota.id,
                                        onSuccess = {
                                            Log.w("ADMIN", "bien-------------------------------------------------")
                                            mascotas = mascotas.filterNot { it.id == mascota.id }
                                        },
                                        onError = { Log.e("ManagePets", "Error", it) }
                                    )
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
