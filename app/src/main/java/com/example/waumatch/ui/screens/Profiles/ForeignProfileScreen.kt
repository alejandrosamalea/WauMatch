package com.example.waumatch.ui.screens.Profiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.ForeignKey
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@Composable
fun ForeignProfileScreen(userId: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("Sin nombre") }
    var fechaRegistro by remember { mutableStateOf("01/2025") }
    var subtitle by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Añade una descripción") }
    var availability by remember {
        mutableStateOf(
            mapOf(
                "Lunes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Martes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Miércoles" to mapOf("start" to "00:00", "end" to "24:00"),
                "Jueves" to mapOf("start" to "00:00", "end" to "24:00"),
                "Viernes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Sábado" to mapOf("start" to "00:00", "end" to "24:00"),
                "Domingo" to mapOf("start" to "00:00", "end" to "24:00")
            )
        )
    }
    var profileImage by remember { mutableStateOf("https://via.placeholder.com/150") }
    var tags by remember { mutableStateOf(listOf("♥️ Amante de los animales")) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        val usuario = db.collection("usuarios").document(userId)
        usuario.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    nombre = documentSnapshot.getString("nombre") ?: "Sin nombre"
                    fechaRegistro = documentSnapshot.getString("fechaRegistro") ?: "01/2025"
                    subtitle = documentSnapshot.getString("subtitle") ?: ""
                    about = documentSnapshot.getString("about") ?: "Añade una descripción"
                    tags = documentSnapshot.get("tags") as? List<String> ?: tags
                    profileImage = documentSnapshot.getString("profileImage") ?: "https://via.placeholder.com/150"

                    val firebaseAvailability = documentSnapshot.get("availability")
                    if (firebaseAvailability != null) {
                        when (firebaseAvailability) {
                            is Map<*, *> -> {
                                try {
                                    val availabilityMap = firebaseAvailability as Map<String, Map<String, String>>
                                    availability = availability.toMutableMap().apply {
                                        availabilityMap.forEach { (day, times) ->
                                            if (this.containsKey(day)) {
                                                this[day] = times
                                            }
                                        }
                                    }
                                } catch (e: ClassCastException) {
                                    val oldAvailability = firebaseAvailability as Map<String, String>
                                    availability = availability.toMutableMap().apply {
                                        oldAvailability.forEach { (day, timeRange) ->
                                            if (this.containsKey(day)) {
                                                val times = timeRange.split(" - ").let {
                                                    if (it.size == 2) mapOf("start" to it[0], "end" to it[1])
                                                    else mapOf("start" to "00:00", "end" to "24:00")
                                                }
                                                this[day] = times
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                println("Formato de availability no soportado: $firebaseAvailability")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error al cargar datos: ${exception.message}")
            }
    }


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
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = ComposeColor.White
            )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = ComposeColor.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImage),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ComposeColor(0xFF2EDFF2), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        color = ComposeColor(0xFF1EB7D9)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .background(
                                color = ComposeColor(0x1A1EB7D9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(number = "4.9", label = "Rating")
                            StatItem(number = "127", label = "Cuidados")
                            val (mesReg, anioReg) = fechaRegistro.split("/").map { it.toInt() }
                            val totalMeses = (Calendar.getInstance().get(Calendar.YEAR) - anioReg) * 12 + (Calendar.getInstance().get(Calendar.MONTH) + 1 - mesReg)
                            StatItem(
                                number = if (totalMeses >= 12) (totalMeses / 12).toString() else totalMeses.toString(),
                                label = if (totalMeses >= 12) if (totalMeses / 12 == 1) "Año" else "Años" else if (totalMeses == 1) "Mes" else "Meses"
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Sobre Mí",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = about,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = ComposeColor.White,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .background(ComposeColor(0x262EDFF2), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 14.sp,
                                    color = ComposeColor(0xFF2EDFF2)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Disponibilidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    availability.forEach { (day, times) ->
                        AvailabilityItem(
                            day = day,
                            startTime = times["start"] ?: "00:00",
                            endTime = times["end"] ?: "24:00",
                            isEditing = false,
                            onTimeChange = { _, _ -> }
                        )
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Últimas Reseñas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    Review(
                        reviewerImageUrl = "https://api.a0.dev/assets/image?text=happy%20person%20avatar&aspect=1:1",
                        reviewerName = "Carlos P.",
                        rating = 5,
                        reviewText = "Excelente cuidadora. Mi perro regresó muy feliz y bien cuidado.",
                        onClick = {  }
                    )
                }
            }
        }
        item {
            Button(
                onClick = { /* Acción de contacto */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ComposeColor(0xFF2EDFF2),
                    contentColor = ComposeColor(0xFF111826)
                )
            ) {
                Text(
                    text = "Contactar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

