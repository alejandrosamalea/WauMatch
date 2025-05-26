package com.example.waumatch.ui.screens

import android.app.Application
import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.example.waumatch.viewmodel.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import androidx.compose.material.icons.outlined.Image
import com.example.waumatch.ui.components.Mascota
import com.example.waumatch.ui.screens.Profiles.ProfileManagerFactory
import com.google.firebase.firestore.toObject
import java.util.*

@Composable
fun AddScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))
    val profileModel : ProfileManager = viewModel(factory = ProfileManagerFactory(LocalContext.current))
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var creador by remember { mutableStateOf("") }
    var idCreador by remember { mutableStateOf("") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val MAX_TITULO = 30
    val MAX_DESCRIPCION = 250
    val imageUris = remember { mutableStateListOf<String?>(null, null, null) }
    val mascotas = remember { mutableStateListOf<Mascota>() }
    val mascotasSeleccionadas = remember { mutableStateListOf<String>() }
    var tipoAnuncio by remember { mutableStateOf("Dueño") }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context2 = LocalContext.current

    // DatePicker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = "$dayOfMonth/${month + 1}/$year"
            if (showStartPicker) {
                fechaInicio = formattedDate
                showStartPicker = false
            } else if (showEndPicker) {
                fechaFin = formattedDate
                showEndPicker = false
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("usuarios").document(userId)
            val mascotasSnapshot = db.collection("mascotas").document(userId).collection("lista").get().await()

            mascotas.clear()
            mascotas.addAll(
                mascotasSnapshot.documents.mapNotNull {
                    doc -> doc.toObject(Mascota::class.java)?.copy(id = doc.id)
                }
            )
            try {
                val userDocument = userRef.get().await()
                val nombreUsuario = userDocument.getString("nombre") ?: "Usuario Anónimo"
                idCreador = userId
                Log.d("NAVEGACION", "id Creador: ${idCreador}")
                creador = nombreUsuario
                Log.d("NAVEGACION", "Nombre Creador: ${creador}")
            } catch (e: Exception) {
                creador = "Usuario Anónimo"
            }
        }
    }

    val pickImageLaunchers = List(3) { index ->
        rememberLauncherForActivityResult(

            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                imageUris[index] = it.toString()
            }
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Start)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            // Título "Detalles del anuncio"
            Text(
                text = "Detalles del anuncio",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = "Tipo de anuncio*",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Dueño", "Paseador").forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { tipoAnuncio = tipo }
                    ) {
                        RadioButton(
                            selected = tipoAnuncio == tipo,
                            onClick = { tipoAnuncio = tipo },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.White)
                        )
                        Text(
                            text = tipo,
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(horizontal = 4.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    pickImageLaunchers[index].launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (imageUris[index] != null) {
                                AsyncImage(
                                    model = imageUris[index],
                                    contentDescription = "Imagen seleccionada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Image,
                                    contentDescription = "Añadir imagen",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (index == 0) {
                                Text(
                                    text = "Foto principal",
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Título*",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { if (it.length <= MAX_TITULO) titulo = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp)),
                placeholder = { Text("Título del anuncio", color = Color(0xFF999999)) },
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                singleLine = true
            )
            Text(
                text = "${titulo.length}/$MAX_TITULO",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Descripción*",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= MAX_DESCRIPCION) descripcion = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                placeholder = { Text("Describe los detalles de tu anuncio", color = Color(0xFF999999)) },
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                maxLines = 4
            )
            Text(
                text = "${descripcion.length}/$MAX_DESCRIPCION",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (tipoAnuncio == "Dueño") {
                Text(
                    text = "Selecciona las mascotas para este anuncio",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (mascotas.size > 0) {
                    mascotas.forEach { mascota ->
                        val seleccionada = mascotasSeleccionadas.contains(mascota.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (seleccionada) mascotasSeleccionadas.remove(mascota.id)
                                    else mascotasSeleccionadas.add(mascota.id)
                                }
                                .padding(vertical = 4.dp)
                                .background(
                                    if (seleccionada) Color(0xFFB3E5FC) else Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = mascota.nombre,
                                color = Color.Black,
                                fontSize = 12.sp,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Actualmente no tienes mascotas registradas, hazlo en tu perfil",
                        color = Color.Black,
                        fontSize = 16.sp,
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha Inicio*",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { if (fechaInicio.isEmpty()) fechaInicio = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        readOnly = true,
                        placeholder = { Text(formatDate(calendar.time), color = Color(0xFF999999)) },
                        trailingIcon = {
                            IconButton(onClick = { showStartPicker = true; datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = Color.Black)
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha Fin*",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { if (fechaFin.isEmpty()) fechaFin = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        readOnly = true,
                        placeholder = { Text(formatDate(calendar.time), color = Color(0xFF999999)) },
                        trailingIcon = {
                            IconButton(onClick = { showEndPicker = true; datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = Color.Black)
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))
            Button(
                onClick = {
                    if (titulo.isBlank() || descripcion.isBlank() || fechaInicio.isBlank() || fechaFin.isBlank()) {
                        Toast.makeText(context, "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (descripcion.length > MAX_DESCRIPCION) {
                        Toast.makeText(context, "La descripción es demasiado larga", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (titulo.length > MAX_TITULO) {
                        Toast.makeText(context, "El título es demasiado largo", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val start = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).parse(fechaInicio)
                    val end = SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).parse(fechaFin)
                    if (start != null && end != null && start > end) {
                        Toast.makeText(context, "La fecha de inicio debe ser anterior a la fecha de fin", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val nuevoAnuncio = AnuncioEntity(
                        id = UUID.randomUUID().toString(),
                        titulo = titulo,
                        descripcion = descripcion,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        creador = creador,
                        idCreador = idCreador,
                        esFavorito = false,
                        imagenes = imageUris.filterNotNull(),
                        tipo = tipoAnuncio,
                        mascotasIds = mascotasSeleccionadas.toList()
                    )

                    viewModel.agregarAnuncio(nuevoAnuncio, context2)
                    Toast.makeText(context, "Anuncio creado correctamente", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2EDFF2))
            ) {
                Text(
                    text = "Subir anuncio",
                    color = Color(0xFF111826),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatDate(date: Date): String {
    return android.text.format.DateFormat.format("dd/M/yyyy", date).toString()
}