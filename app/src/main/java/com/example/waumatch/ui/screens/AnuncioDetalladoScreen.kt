package com.example.waumatch.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R
import com.example.waumatch.ui.components.Mascota
import com.example.waumatch.ui.screens.Profiles.ProfileManagerFactory
import com.example.waumatch.ui.screens.Profiles.ReviewData
import com.example.waumatch.ui.screens.Profiles.loadTopReviews
import com.example.waumatch.ui.theme.AquaLight
import com.example.waumatch.ui.theme.NightBlue
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.example.waumatch.viewmodel.ProfileManager
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AnuncioDetalladoScreen(
    navController: NavController,
    anuncioId: String,
    onBackClick: () -> Unit
) {


    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))
    val userId2 = FirebaseAuth.getInstance().currentUser?.uid
    val anuncio = viewModel.getAnuncioById(anuncioId).collectAsState(initial = null).value
    val auth = FirebaseAuth.getInstance()
    var localProfileImage by remember { mutableStateOf<String?>(null) }

    val profileManager: ProfileManager = viewModel(factory = ProfileManagerFactory(context))
    val profileData by profileManager.getProfileDataExt(anuncio?.idCreador ?: "1").observeAsState(ProfileManager.ProfileData())

    var reviews by remember { mutableStateOf(listOf<ReviewData>()) }
    val reviewCount = reviews.size
    val averageRating = if (reviews.isNotEmpty()) {
        String.format("%.1f", reviews.map { it.rating }.average())
    } else {
        "0.0"
    }

    val profileImage = localProfileImage ?: profileData.profileImage.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150"
    val mascotas = remember { mutableStateListOf<Mascota>() }

    LaunchedEffect(anuncio?.id) {
        if (anuncio != null && anuncio.mascotasIds.isNotEmpty()) {
            viewModel.getMascotasByIds(anuncio.idCreador, anuncio.mascotasIds) { lista ->
                mascotas.clear()
                mascotas.addAll(lista)
            }
        }
    }
    if (anuncio != null) {
        LaunchedEffect(anuncio.idCreador) {
            loadTopReviews(anuncio.idCreador) { fetchedReviews ->
                reviews = fetchedReviews
            }
        }
    }

    if (anuncio == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AquaLight)
        }
    } else {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OceanBlue)
        ) {



            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp)
            ) {

                // Carrusel de imágenes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    val images = anuncio.imagenes.ifEmpty { listOf("https://via.placeholder.com/150") }
                    val pagerState = rememberPagerState(pageCount = { images.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Image(
                            painter = rememberAsyncImagePainter(model = images[page]),
                            contentDescription = "Imagen del anuncio ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Indicador de puntos
                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(images.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) Color.White
                                            else Color.White.copy(alpha = 0.4f)
                                        )
                                )
                            }
                        }
                    }

                    // Botón de volver
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(NightBlue.copy(alpha = 0.7f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    // Botones de favorito y compartir
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        if (anuncio.idCreador != userId2) {
                        IconButton(
                            onClick = { viewModel.toggleFavorito(anuncio) },
                            modifier = Modifier
                                .background(NightBlue.copy(alpha = 0.7f), shape = CircleShape)
                        ) {
                            val isFav = anuncio.esFavorito
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFav) "Desmarcar favorito" else "Marcar favorito",
                                tint = if (isFav) Color.Red else Color.White
                            )
                        }
                    }
                    }
                }

                // Resto del contenido
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OceanBlue)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = anuncio.titulo,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = " ${anuncio.tipos}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable {
                                Log.d("NAVEGACION", "Navegando a perfil de: ${anuncio.idCreador}")
                                navController.navigate("foreignProfile/${anuncio.idCreador}")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = profileImage),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = anuncio.creador,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$averageRating ($reviewCount ${if (reviewCount == 1) "valoración" else "valoraciones"})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )


                    Text(
                        text = anuncio.descripcion,
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )

                    if (mascotas.isNotEmpty()) {
                        Divider(
                            color = Color.White.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Mascotas asociadas al anuncio",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(mascotas) { mascota ->

                                Column(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OceanBlue)
                                        .clickable {
                                            navController.navigate("mascotaDetailsScreen/${mascota.idDuenio}/${mascota.id}")
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = if (mascota.imagenes.isNotEmpty()) mascota.imagenes[0] else "https://via.placeholder.com/80",
                                            placeholder = painterResource(id = R.drawable.profile)
                                        ),
                                        contentDescription = "${mascota.nombre} image",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, (OceanBlue), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = mascota.nombre,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                        }
                    }

                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )


                    Text(
                        text = "Disponibilidad:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Desde: ${anuncio.fechaInicio}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Hasta: ${anuncio.fechaFin}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }

            if (anuncio.idCreador != userId2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(OceanBlue)
            ) {
                Divider(
                    color = Color.White.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Button(
                    onClick = { navController.navigate("chatDetail/${anuncio.idCreador}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AquaLight
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Contactar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = NightBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        }
    }
}