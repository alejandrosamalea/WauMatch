package com.example.waumatch.ui.screens.mascotas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.waumatch.data.MascotaRepository
import com.example.waumatch.ui.components.Mascota
import com.example.waumatch.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMascota(
    navController: NavController,
    repository: MascotaRepository = MascotaRepository()
) {
    var mascotas by remember { mutableStateOf<List<Mascota>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.obtenerMascotasDelUsuario(
            onSuccess = { mascotas = it },
            onError = { android.util.Log.e("ManagePets", "Error", it) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OceanBlue, SkyBlue)
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    "Mis Mascotas",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NightBlue,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (mascotas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OceanBlue.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Actualmente no tienes mascotas registradas",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp)
            ) {
                items(mascotas) { mascota ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .shadow(6.dp, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = DeepNavy
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            AsyncImage(
                                model = mascota.imagenes.firstOrNull() ?: "https://via.placeholder.com/150",
                                contentDescription = "Imagen de la mascota",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        navController.navigate("mascotaDetailsScreen/${mascota.idDuenio}/${mascota.id}")
                                    },
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                error = painterResource(android.R.drawable.ic_menu_gallery)
                            )
                            Text(
                                text = "Nombre: ${mascota.nombre}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Especie: ${mascota.especie}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Raza: ${mascota.raza}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Edad: ${mascota.edad}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        navController.navigate("editarmascota/${mascota.id}")
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(AquaLight, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        repository.eliminarMascota(
                                            mascota.id,
                                            onSuccess = {
                                                mascotas =
                                                    mascotas.filterNot { it.id == mascota.id }
                                            },
                                            onError = {
                                                android.util.Log.e(
                                                    "ManagePets",
                                                    "Error",
                                                    it
                                                )
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(AquaLight, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.White
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
