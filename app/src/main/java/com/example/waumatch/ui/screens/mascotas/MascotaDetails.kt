package com.example.waumatch.ui.screens.mascotas

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import androidx.compose.material3.Text
import com.example.waumatch.ui.components.Mascota

@Composable
fun MascotaDetailsScreen(
    navController: NavController,
    mascotaId: String,
    userId: String
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val mascota = remember { mutableStateOf<Mascota?>(null) }

    // Cargar datos de la mascota desde Firestore
    LaunchedEffect(userId, mascotaId) {
        if (userId.isNotBlank() && mascotaId.isNotBlank()) {
            try {
                val document = db.collection("mascotas")
                    .document(userId)
                    .collection("lista")
                    .document(mascotaId)
                    .get()
                    .await()
                if (document.exists()) {
                    mascota.value = document.toObject(Mascota::class.java)?.copy(id = document.id)
                } else {
                    Log.e("MascotaDetailsScreen", "Documento de mascota no encontrado")
                }
            } catch (e: Exception) {
                Log.e("MascotaDetailsScreen", "Error al cargar datos de la mascota: ${e.message}")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OceanBlue, SkyBlue)
                )
            )
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón de volver atrás
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(OceanBlue.copy(alpha = 0.9f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ComposeColor.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                mascota.value?.let { pet ->
                    // Imagen de la mascota
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = if (pet.imagenes.isNotEmpty()) pet.imagenes[0] else "https://via.placeholder.com/150",
                            placeholder = painterResource(id = com.example.waumatch.R.drawable.profile)
                        ),
                        contentDescription = "${pet.nombre} image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ComposeColor(0xFF2EDFF2), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    // Nombre de la mascota
                    Text(
                        text = pet.nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Sección de detalles
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                            .padding(15.dp)
                    ) {
                        DetailItem("Especie", pet.especie)
                        DetailItem("Raza", pet.raza)
                        DetailItem("Edad", "${pet.edad} años")
                        DetailItem("Acciones no toleradas", pet.accionesNoToleradas)
                        DetailItem("Información adicional", pet.adicional)
                        DetailItem("Ansiedad por separación", pet.ansiedadSeparacion)
                        DetailItem("Contacto alternativo", pet.contactoAlternativo)
                        DetailItem("Correo del dueño", pet.correoDuenio)
                        DetailItem("Escapa", pet.escapa)
                        DetailItem("Frecuencia de paseo", pet.frecuenciaPaseo)
                        DetailItem("Habitaciones restringidas", pet.habitacionesRestringidas)
                        DetailItem("Hábitos de higiene", pet.habitosHigiene)
                        DetailItem("Horario de dormir", pet.horarioDormir)
                        DetailItem("Horarios de comida", pet.horariosComida)
                        DetailItem("Juguetes favoritos", pet.juguetesFavoritos)
                        DetailItem("Limpieza", pet.limpieza)
                        DetailItem("Lugar de dormir", pet.lugarDormir)
                        DetailItem("Medicación", pet.medicacion)
                        DetailItem("Productos especiales", pet.productosEspeciales)
                        DetailItem("Puede quedarse solo", pet.puedeQuedarseSolo)
                        DetailItem("Restricciones", pet.restricciones)
                        DetailItem("Rituales de comida", pet.ritualesComida)
                        DetailItem("Teléfono del dueño", pet.telefonoDuenio)
                        DetailItem("Tipo de comida", pet.tipoComida)
                        DetailItem("Veterinario", pet.veterinario)
                    }
                } ?: run {
                    // Mostrar mientras se carga la mascota
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cargando datos de la mascota...",
                            color = ComposeColor.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String?) {
    if (value?.isNotBlank() == true) { // Solo renderiza si el valor no es nulo ni está en blanco
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ComposeColor.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = ComposeColor(0xFF2EDFF2),
                lineHeight = 20.sp
            )
        }
    }
}