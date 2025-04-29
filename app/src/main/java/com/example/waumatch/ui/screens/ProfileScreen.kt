package com.example.waumatch.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.example.waumatch.viewmodel.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ProfileScreen(viewModel: ProfileManager = viewModel(factory = ProfileManagerFactory(LocalContext.current))) {
    val context = LocalContext.current
    val isOwnProfile = true
    val profileData by viewModel.getProfileData().observeAsState(ProfileManager.ProfileData())

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
    var localProfileImage by remember { mutableStateOf<String?>(null) }
    var tags by remember { mutableStateOf(listOf("♥️ Amante de los animales")) }
    var newTag by remember { mutableStateOf("") }
    val isEditing by viewModel.getIsEditing().observeAsState(false)

    val MAX_NAME_LENGTH = 15
    val MAX_SUBTITLE_LENGTH = 10
    val MAX_ABOUT_LENGTH = 300
    val MAX_TAGS = 4

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser != null) {
        LaunchedEffect(currentUser.uid) {
            val usuario = db.collection("usuarios").document(currentUser.uid)
            usuario.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        nombre = documentSnapshot.getString("nombre") ?: "Sin nombre"
                        fechaRegistro = documentSnapshot.getString("fechaRegistro") ?: "01/2025"
                        subtitle = documentSnapshot.getString("subtitle") ?: ""
                        about = documentSnapshot.getString("about") ?: "Añade una descripción"
                        tags = documentSnapshot.get("tags") as? List<String> ?: tags

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
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            localProfileImage = it.toString()
            if (currentUser != null) {
                viewModel.updateProfileImage(it.toString()) // No es necesario el indicador de carga
            }
        }
    }
    val profileImage = localProfileImage ?: profileData.profileImage.takeIf { it.isNotEmpty() } ?: "https://via.placeholder.com/150"

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
                    .padding(top = 60.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                if (isOwnProfile) {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                if (currentUser != null) {
                                    viewModel.saveChanges(nombre, subtitle, about, availability,tags)
                                }
                            } else {
                                viewModel.toggleEditing()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 20.dp)
                            .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(20.dp))
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            tint = ComposeColor(0xFF2EDFF2)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(profileImage),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(3.dp, ComposeColor(0xFF2EDFF2), CircleShape)
                                .run {
                                    if (isEditing) this.alpha(0.8f) else this
                                }
                                .clickable(enabled = isEditing) {
                                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    if (isEditing) {
                        TextField(
                            value = nombre,
                            onValueChange = { if (it.length <= MAX_NAME_LENGTH) nombre = it },
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            ),
                            placeholder = { Text("Tu nombre", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                        Text(
                            text = "${nombre.length}/$MAX_NAME_LENGTH",
                            fontSize = 12.sp,
                            color = ComposeColor(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = nombre,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComposeColor.White
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    if (isEditing) {
                        TextField(
                            value = subtitle,
                            onValueChange = { if (it.length <= MAX_SUBTITLE_LENGTH) subtitle = it },
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor(0xFF1EB7D9),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            placeholder = { Text("Tu descripción corta", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                        Text(
                            text = "${subtitle.length}/$MAX_SUBTITLE_LENGTH",
                            fontSize = 12.sp,
                            color = ComposeColor(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = subtitle,
                            fontSize = 16.sp,
                            color = ComposeColor(0xFF1EB7D9)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(number = "4.9", label = "Rating")
                        StatItem(number = "127", label = "Cuidados")

                        val (mesReg, anioReg) = fechaRegistro.split("/").map { it.toInt() }
                        val totalMeses = (Calendar.getInstance().get(Calendar.YEAR) - anioReg) * 12 + (Calendar.getInstance().get(Calendar.MONTH) + 1 - mesReg)
                        StatItem(number = if (totalMeses >= 12) (totalMeses / 12).toString() else totalMeses.toString(), label = if (totalMeses >= 12) if (totalMeses / 12 == 1) "Año" else "Años" else if (totalMeses == 1) "Mes" else "Meses")
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
                    if (isEditing) {
                        TextField(
                            value = about,
                            onValueChange = { if (it.length <= MAX_ABOUT_LENGTH) about = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor.White,
                                fontSize = 15.sp
                            ),
                            placeholder = { Text("Cuéntanos sobre ti...", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                        Text(
                            text = "${about.length}/$MAX_ABOUT_LENGTH",
                            fontSize = 12.sp,
                            color = ComposeColor(0xFF666666),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = about,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = ComposeColor.White,
                            modifier = Modifier.padding(bottom = 15.dp)
                        )
                    }
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .background(ComposeColor(0x262EDFF2), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .clickable(enabled = isEditing) { tags = tags - tag }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 14.sp,
                                    color = ComposeColor(0xFF2EDFF2)
                                )
                            }
                        }
                    }
                    if (isEditing && tags.size < MAX_TAGS) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = newTag,
                                onValueChange = { newTag = it },
                                placeholder = { Text("Añadir nueva etiqueta", color = ComposeColor(0xFF666666)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                                textStyle = LocalTextStyle.current.copy(
                                    color = ComposeColor.White,
                                    fontSize = 14.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = ComposeColor.Transparent,
                                    unfocusedContainerColor = ComposeColor.Transparent,
                                    focusedIndicatorColor = ComposeColor.Transparent,
                                    unfocusedIndicatorColor = ComposeColor.Transparent
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (newTag.trim().isNotEmpty()) {
                                        tags = tags + newTag.trim()
                                        newTag = ""
                                    }
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add tag",
                                    tint = ComposeColor(0xFF2EDFF2)
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
                            isEditing = isEditing,
                            onTimeChange = { start, end ->
                                availability = availability.toMutableMap().apply {
                                    this[day] = mapOf("start" to start, "end" to end)
                                }
                            }
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
                        reviewText = "Excelente cuidadora. Mi perro regresó muy feliz y bien cuidado."
                    )
                }
            }
        }
        if (!isOwnProfile) {
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
}

// Factory para crear ProfileManager
class ProfileManagerFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileManager(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Componentes auxiliares
@Composable
fun StatItem(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ComposeColor(0xFF2EDFF2)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = ComposeColor.White,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun AvailabilityItem(
    day: String,
    startTime: String,
    endTime: String,
    isEditing: Boolean,
    onTimeChange: (String, String) -> Unit
) {
    val context = LocalContext.current
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    var selectingStart by remember { mutableStateOf(true) }

    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                onTimeSelected(timeFormatter.format(newCalendar.time))
            },
            hour,
            minute,
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day,
                color = ComposeColor.White,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (isEditing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = startTime,
                        color = ComposeColor(0xFF2EDFF2),
                        modifier = Modifier
                            .width(70.dp)
                            .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable {
                                selectingStart = true
                                showTimePicker { newStartTime ->
                                    try {
                                        val startDate = timeFormatter.parse(newStartTime)
                                        val endDate = timeFormatter.parse(endTime)
                                        if (endDate != null && startDate != null && startDate.before(endDate)) {
                                            onTimeChange(newStartTime, endTime)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "La hora de inicio debe ser anterior a la de fin",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error al procesar la hora",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "-",
                        color = ComposeColor.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = endTime,
                        color = ComposeColor(0xFF2EDFF2),
                        modifier = Modifier
                            .width(70.dp)
                            .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .clickable {
                                selectingStart = false
                                showTimePicker { newEndTime ->
                                    try {
                                        val startDate = timeFormatter.parse(startTime)
                                        val endDate = timeFormatter.parse(newEndTime)
                                        if (endDate != null && startDate != null && startDate.before(endDate)) {
                                            onTimeChange(startTime, newEndTime)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "La hora de fin debe ser posterior a la de inicio",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Error al procesar la hora",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    text = "$startTime - $endTime",
                    color = ComposeColor(0xFF2EDFF2),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun Review(reviewerImageUrl: String, reviewerName: String, rating: Int, reviewText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = rememberAsyncImagePainter(reviewerImageUrl),
            contentDescription = "Reviewer Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, ComposeColor.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(
                text = reviewerName,
                fontWeight = FontWeight.Bold,
                color = ComposeColor.White,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = ComposeColor(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "\"$reviewText\"",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = ComposeColor.White
            )
        }
    }
}