package com.example.waumatch.ui.screens.Profiles

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import androidx.navigation.NavController
import com.example.waumatch.MainActivity
import com.example.waumatch.R
import com.example.waumatch.ui.components.Mascota
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import android.Manifest
import android.app.Application
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.waumatch.viewmodel.AnuncioViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileManager = viewModel(factory = ProfileManagerFactory(LocalContext.current)),
    anuncioViewModel: AnuncioViewModel
) {
    val context = LocalContext.current
    val profileData by viewModel.getProfileData().observeAsState(ProfileManager.ProfileData())

    var locationText by remember { mutableStateOf("Ubicación no disponible") }
    var isLocationPermissionGranted by remember { mutableStateOf(false) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var nombre by remember { mutableStateOf("Sin nombre") }
    var fechaRegistro by remember { mutableStateOf("01/2025") }
    var telefono by remember { mutableStateOf("No disponible") }
    var subtitle by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Añade una descripción") }
    val application = context.applicationContext as Application
    val anuncios = anuncioViewModel.anuncios.collectAsState().value

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
    var reviews by remember { mutableStateOf(listOf<ReviewData>()) }
    val isEditing by viewModel.getIsEditing().observeAsState(false)

    val MAX_NAME_LENGTH = 15
    val MAX_SUBTITLE_LENGTH = 10
    val MAX_ABOUT_LENGTH = 300
    val MAX_TAGS = 4

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val averageRating = if (reviews.isNotEmpty()) {
        String.format("%.1f", reviews.map { it.rating }.average())
    } else {
        "0.0"
    }
    val reviewCount = reviews.size

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val NAnunciosUsuario = anuncios.count { it.idCreador == currentUser?.uid ?: 0 }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
        if (isGranted) {
            getLastKnownLocation(fusedLocationClient, context) { address ->
                locationText = address
                if (currentUser != null) {
                    db.collection("usuarios").document(currentUser.uid)
                        .update("location", address)
                        .addOnFailureListener { e ->
                            Log.e("ProfileScreen", "Error al guardar ubicación: ${e.message}")
                        }
                }
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar datos del perfil
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
                        locationText = documentSnapshot.getString("location") ?: "Ubicación no disponible"
                        telefono = documentSnapshot.getString("telefono") ?: "No disponible"

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



    // Cargar reseñas
        LaunchedEffect(currentUser.uid) {
            loadTopReviews(currentUser.uid) { fetchedReviews ->
                reviews = fetchedReviews
            }
        }

        // Verificar permisos y actualizar ubicación si es necesario
        LaunchedEffect(Unit) {
            isLocationPermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (isLocationPermissionGranted && locationText == "Ubicación no disponible") {
                getLastKnownLocation(fusedLocationClient, context) { address ->
                    locationText = address
                    db.collection("usuarios").document(currentUser.uid)
                        .update("location", address)
                        .addOnFailureListener { e ->
                            Log.e("ProfileScreen", "Error al guardar ubicación: ${e.message}")
                        }
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            localProfileImage = it.toString()
            if (currentUser != null) {
                viewModel.updateProfileImage(it.toString())
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
                    .padding(top = 20.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = "Cerrar sesión", color = ComposeColor(0xFF2EDFF2))
                }
                IconButton(
                    onClick = {
                        if (isEditing) {
                            if (currentUser != null) {
                                viewModel.saveChanges(nombre, subtitle, about, availability, tags)
                                db.collection("usuarios").document(currentUser.uid)
                                    .update(mapOf(
                                        "location" to locationText,
                                        "telefono" to telefono // Añadir el teléfono al mapa de actualización
                                    )
                                    )
                                    .addOnFailureListener { e ->
                                        Log.e("ProfileScreen", "Error al guardar ubicación: ${e.message}")
                                    }
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
                            color = ComposeColor.White,
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
                                color = ComposeColor.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            placeholder = { Text("Tu descripción corta", color = ComposeColor.White) },
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
                            color = ComposeColor.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = subtitle,
                            fontSize = 16.sp,
                            color = ComposeColor(0xFF1EB7D9)
                        )
                    }
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = averageRating,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ComposeColor(0xFF2EDFF2)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Average Rating Star",
                                        tint = ComposeColor(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = if (reviewCount == 1) "$reviewCount reseña" else "$reviewCount reseñas",
                                    fontSize = 14.sp,
                                    color = ComposeColor.White,
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                            StatItem(
                                number = NAnunciosUsuario.toString(),
                                label = "Anuncios",
                                modifier = Modifier.clickable {
                                    Log.i("ADMIN", "clicka")
                                    navController.navigate("misAnuncios")
                                }
                            )

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
                            color = ComposeColor.White,
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
                                placeholder = { Text("Añadir nueva etiqueta", color = ComposeColor.White) },
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
                    text = "Teléfono de contacto",
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
                    if (isEditing) {
                        val MAX_PHONE_LENGTH = 9 // Máximo y mínimo 9 dígitos
                        var isPhoneValid by remember { mutableStateOf(telefono.length == MAX_PHONE_LENGTH && telefono.matches(Regex("^[0-9]{9}$"))) }

                        TextField(
                            value = telefono,
                            onValueChange = { newValue ->
                                // Solo permitir números y máximo 9 dígitos
                                if (newValue.length <= MAX_PHONE_LENGTH && newValue.matches(Regex("^[0-9]*$"))) {
                                    telefono = newValue
                                    isPhoneValid = newValue.length == MAX_PHONE_LENGTH
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (isPhoneValid || telefono.isEmpty()) ComposeColor(0xFF2EDFF2) else ComposeColor.Red,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor.White,
                                fontSize = 16.sp
                            ),
                            placeholder = { Text("Ingresa tu número de teléfono", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Teclado numérico
                            isError = !isPhoneValid && telefono.isNotEmpty() // Mostrar error si no es válido
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${telefono.length}/$MAX_PHONE_LENGTH",
                                fontSize = 12.sp,
                                color = ComposeColor.White
                            )
                            if (!isPhoneValid && telefono.isNotEmpty()) {
                                Text(
                                    text = "Debe tener exactamente 9 dígitos",
                                    fontSize = 12.sp,
                                    color = ComposeColor.Red
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Teléfono",
                                tint = ComposeColor(0xFF2EDFF2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = telefono,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (telefono == "No disponible") ComposeColor.Gray else ComposeColor.White
                            )
                        }
                    }
                }
            }
        }
        item {
            val mascotas = remember { mutableStateListOf<Mascota>() }
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            LaunchedEffect(userId) {
                if (userId.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    try {
                        val mascotasSnapshot = db.collection("mascotas")
                            .document(userId)
                            .collection("lista")
                            .get()
                            .await()
                        mascotas.clear()
                        mascotas.addAll(
                            mascotasSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Mascota::class.java)?.copy(id = doc.id)
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error al cargar mascotas: ${e.message}")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mis Mascotas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White
                    )
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Añadir mascota",
                                tint = ComposeColor(0xFF2EDFF2)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(ComposeColor(0xFF1A1EB7D9))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Agregar mascota", color = ComposeColor.White) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("anadirMascota")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gestionar mascotas", color = ComposeColor.White) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("adminMascota")
                                }
                            )
                        }
                    }
                }

                if (mascotas.isNotEmpty()) {
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
                                    .background(
                                        color = ComposeColor(0x1A1EB7D9),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        navController.navigate("adminMascota")
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
                                        .border(1.dp, ComposeColor(0xFF2EDFF2), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = mascota.nombre,
                                    color = ComposeColor.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No tienes mascotas registradas",
                        color = ComposeColor.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ubicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White
                    )
                    if (!isLocationPermissionGranted) {
                        IconButton(onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Solicitar ubicación",
                                tint = ComposeColor(0xFF2EDFF2)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = locationText,
                            color = ComposeColor.White,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isEditing) {
                            Button(
                                onClick = {
                                    if (isLocationPermissionGranted) {
                                        getLastKnownLocation(fusedLocationClient, context) { address ->
                                            locationText = address
                                            if (currentUser != null) {
                                                db.collection("usuarios").document(currentUser.uid)
                                                    .update("location", address)
                                                    .addOnFailureListener { e ->
                                                        Log.e("ProfileScreen", "Error al actualizar ubicación: ${e.message}")
                                                    }
                                            }
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFF2EDFF2))
                            ) {
                                Text("Actualizar", color = ComposeColor.White, fontSize = 14.sp)
                            }
                        }
                    }
                    // Añadir texto "Ver mapa" debajo
                    TextButton(
                        onClick = { navController.navigate("test") },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Ver mapa",
                            color = ComposeColor(0xFF2EDFF2),
                            fontSize = 12.sp
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
                    if (reviews.isEmpty()) {
                        Text(
                            text = "Aún no tienes reseñas",
                            fontSize = 14.sp,
                            color = ComposeColor.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        reviews.forEach { review ->
                            Review(
                                reviewerImageUrl = review.reviewerImageUrl,
                                reviewerName = review.reviewerName,
                                rating = review.rating,
                                reviewText = review.comment,
                                onClick = { navController.navigate("foreignProfile/${review.idEmisor}") }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                TextButton(
                    onClick = {
                        navController.navigate("allReviews/${currentUser?.uid}")
                    },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Ver todas las reseñas",
                        color = ComposeColor(0xFF2EDFF2),
                        fontSize = 16.sp
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

@Composable
fun StatItem(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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

fun getLastKnownLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: android.content.Context,
    onLocationReceived: (String) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressText = address.locality?.let { "$it, ${address.countryName}" }
                            ?: "Lat: ${location.latitude}, Lon: ${location.longitude}"
                        onLocationReceived(addressText)
                    } else {
                        onLocationReceived("Ubicación no disponible")
                    }
                } catch (e: Exception) {
                    Log.e("ProfileScreen", "Error al obtener dirección: ${e.message}")
                    onLocationReceived("Error al obtener dirección")
                }
            } else {
                onLocationReceived("Ubicación no disponible")
            }
        }.addOnFailureListener { e ->
            Log.e("ProfileScreen", "Error al obtener ubicación: ${e.message}")
            onLocationReceived("Error al obtener ubicación")
        }
    } catch (e: SecurityException) {
        Log.e("ProfileScreen", "Permiso de ubicación no otorgado: ${e.message}")
        onLocationReceived("Permiso de ubicación requerido")
    }
}

@Composable
fun Review(
    reviewerImageUrl: String,
    reviewerName: String,
    rating: Int,
    reviewText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
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