package com.example.waumatch.ui.screens.mascotas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.waumatch.data.MascotaRepository
import com.example.waumatch.ui.components.Mascota
import com.example.waumatch.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMascota(
    navController: NavController,
    mascotaId: String,
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val repository = MascotaRepository()
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var horariosComida by remember { mutableStateOf("") }
    var tipoComida by remember { mutableStateOf("") }
    var ritualesComida by remember { mutableStateOf("") }
    var lugarDormir by remember { mutableStateOf("") }
    var horarioDormir by remember { mutableStateOf("") }
    var habitosHigiene by remember { mutableStateOf("") }
    var frecuenciaPaseo by remember { mutableStateOf("") }
    var limpieza by remember { mutableStateOf("") }
    var productosEspeciales by remember { mutableStateOf("") }
    var juguetesFavoritos by remember { mutableStateOf("") }
    var medicacion by remember { mutableStateOf("") }
    var veterinario by remember { mutableStateOf("") }
    var restricciones by remember { mutableStateOf("") }
    var accionesNoToleradas by remember { mutableStateOf("") }
    var puedeQuedarseSolo by remember { mutableStateOf("") }
    var ansiedadSeparacion by remember { mutableStateOf("") }
    var escapa by remember { mutableStateOf("") }
    var habitacionesRestringidas by remember { mutableStateOf("") }
    var telefonoDuenio by remember { mutableStateOf("") }
    var correoDuenio by remember { mutableStateOf("") }
    var contactoAlternativo by remember { mutableStateOf("") }
    var adicional by remember { mutableStateOf("") }
    val context = LocalContext.current
    val imageUris = remember { mutableStateListOf<String?>(null, null, null) }
    val pickImageLaunchers = List(1) { index ->
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                imageUris[index] = it.toString()
            }
        }
    }
    LaunchedEffect(mascotaId) {
        repository.obtenerMascotaPorId(
            mascotaId,
            onSuccess = { mascota ->
                nombre = mascota.nombre
                especie = mascota.especie
                raza = mascota.raza
                edad = mascota.edad.toString()
                horariosComida = mascota.horariosComida
                tipoComida = mascota.tipoComida
                ritualesComida = mascota.ritualesComida
                lugarDormir = mascota.lugarDormir
                horarioDormir = mascota.horarioDormir
                habitosHigiene = mascota.habitosHigiene
                frecuenciaPaseo = mascota.frecuenciaPaseo
                limpieza = mascota.limpieza
                productosEspeciales = mascota.productosEspeciales
                juguetesFavoritos = mascota.juguetesFavoritos
                medicacion = mascota.medicacion
                veterinario = mascota.veterinario
                restricciones = mascota.restricciones
                accionesNoToleradas = mascota.accionesNoToleradas
                puedeQuedarseSolo = mascota.puedeQuedarseSolo
                ansiedadSeparacion = mascota.ansiedadSeparacion
                escapa = mascota.escapa
                habitacionesRestringidas = mascota.habitacionesRestringidas
                telefonoDuenio = mascota.telefonoDuenio
                correoDuenio = mascota.correoDuenio
                contactoAlternativo = mascota.contactoAlternativo
                adicional = mascota.adicional
                mascota.imagenes?.let {
                    for (i in it.indices) {
                        if (i < imageUris.size) imageUris[i] = it[i]
                    }
                }
            },
            onFailure = {
                Toast.makeText(context, "Error al cargar la mascota", Toast.LENGTH_SHORT).show()
            }
        )
    }
    var isSaving by remember { mutableStateOf(false) }
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
                    "Edita a tu mascota",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ExpandableSection(
                title = "Información básica",
                showMandatory = true
            ) {
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre",
                    isMandatory = true
                )
                DropdownSelector(
                    label = "Especie",
                    options = listOf("Perro", "Gato", "Conejo", "Ave", "Hámster"),
                    selectedOption = especie,
                    onOptionSelected = { especie = it },
                    isMandatory = true
                )
                TextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = "Raza",
                    isMandatory = true
                )
                TextField(
                    value = edad,
                    onValueChange = { edad = it },
                    label = "Edad",
                    keyboardType = KeyboardType.Number,
                    isMandatory = true
                )
            }
            ExpandableSection("Imágenes de la mascota", showMandatory = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(1) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(horizontal = 4.dp)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .background(
                                    color = NightBlue.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (imageUris[index] != null) 2.dp else 0.dp,
                                    color = SkyBlue,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        pickImageLaunchers[index].launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
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
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                if (index == 0) {
                                    Text(
                                        text = "Foto principal",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            ExpandableSection("Rutinas diarias") {
                TextField(horariosComida, { horariosComida = it }, label = "Horarios de comida")
                TextField(tipoComida, { tipoComida = it }, label = "Dosis y tipo de comida")
                TextField(ritualesComida, { ritualesComida = it }, label = "Rituales al comer")
            }
            ExpandableSection("Descanso") {
                TextField(lugarDormir, { lugarDormir = it }, label = "¿Dónde duerme?")
                TextField(horarioDormir, { horarioDormir = it }, label = "¿Tiene horario para dormir?")
            }
            ExpandableSection("Hábitos de higiene") {
                TextField(habitosHigiene, { habitosHigiene = it }, label = "¿Hace pipí/caca en casa?")
                TextField(frecuenciaPaseo, { frecuenciaPaseo = it }, label = "¿Cada cuánto se le saca?")
            }
            ExpandableSection("Limpieza") {
                TextField(limpieza, { limpieza = it }, label = "¿Hay que bañarlo o cepillarlo?")
                TextField(productosEspeciales, { productosEspeciales = it }, label = "¿Usa productos especiales?")
            }
            ExpandableSection("Juguetes y entretenimiento") {
                TextField(juguetesFavoritos, { juguetesFavoritos = it }, label = "Juguetes favoritos")
            }
            ExpandableSection("Salud y emergencias") {
                TextField(medicacion, { medicacion = it }, label = "¿Toma medicación?")
                TextField(veterinario, { veterinario = it }, label = "Veterinario de confianza")
            }
            ExpandableSection("Restricciones") {
                TextField(restricciones, { restricciones = it }, label = "Lugares prohibidos")
                TextField(accionesNoToleradas, { accionesNoToleradas = it }, label = "Acciones que no tolera")
            }
            ExpandableSection("Durante ausencias") {
                TextField(puedeQuedarseSolo, { puedeQuedarseSolo = it }, label = "¿Puede quedarse solo?")
                TextField(ansiedadSeparacion, { ansiedadSeparacion = it }, label = "¿Tiene ansiedad por separación?")
            }
            ExpandableSection("Seguridad en casa") {
                TextField(escapa, { escapa = it }, label = "¿Escapa si la puerta está abierta?")
                TextField(habitacionesRestringidas, { habitacionesRestringidas = it }, label = "Habitaciones restringidas")
            }
            ExpandableSection("Datos del dueño") {
                TextField(telefonoDuenio, { telefonoDuenio = it }, label = "Teléfono")
                TextField(correoDuenio, { correoDuenio = it }, label = "Correo")
                TextField(contactoAlternativo, { contactoAlternativo = it }, label = "Contacto alternativo")
            }
            ExpandableSection("Información adicional") {
                TextArea(adicional, { adicional = it }, "Cuentanos todo lo que creas importante de tu mascota")
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (isSaving) return@Button
                    if (nombre.isBlank() || especie.isBlank() || raza.isBlank() || edad.isBlank() || imageUris[0].isNullOrBlank()) {
                        Toast.makeText(context, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    uploadImagesToCloudinary(
                        mascotaId = mascotaId,
                        imageUris = imageUris.filterNotNull(),
                        onComplete = { urls ->
                            val mascota = Mascota(
                                mascotaId,
                                nombre,
                                especie,
                                raza,
                                edad.toIntOrNull() ?: 0,
                                urls,
                                horariosComida,
                                tipoComida,
                                ritualesComida,
                                lugarDormir,
                                horarioDormir,
                                habitosHigiene,
                                frecuenciaPaseo,
                                limpieza,
                                productosEspeciales,
                                juguetesFavoritos,
                                medicacion,
                                veterinario,
                                restricciones,
                                accionesNoToleradas,
                                puedeQuedarseSolo,
                                ansiedadSeparacion,
                                escapa,
                                habitacionesRestringidas,
                                adicional,
                                userId,
                                telefonoDuenio,
                                correoDuenio,
                                contactoAlternativo
                            )
                            repository.actualizarMascota(
                                mascota,
                                onSuccess = {
                                    Toast.makeText(context, "Mascota guardada con imágenes", Toast.LENGTH_SHORT).show()
                                    navController.navigate("AdminMascota")
                                    isSaving = false
                                },
                                onFailure = { error ->
                                    Log.i("ADMIN", "estoy hasta la polla: ${error.message} - ${mascota.id} ")
                                    Toast.makeText(context, "Error al guardar mascota: ${error.message}", Toast.LENGTH_LONG).show()
                                    isSaving = false
                                }
                            )
                        },
                    )
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AquaLight,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Mascota", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}


@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMandatory: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(if (isMandatory) "$label*" else label, color = Color.White) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AquaLight,
            unfocusedBorderColor = SkyBlue,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            cursorColor = AquaLight,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = NightBlue.copy(alpha = 0.3f),
            unfocusedContainerColor = NightBlue.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 4,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        maxLines = Int.MAX_VALUE,
        singleLine = false,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AquaLight,
            unfocusedBorderColor = SkyBlue,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            cursorColor = AquaLight,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = NightBlue.copy(alpha = 0.3f),
            unfocusedContainerColor = NightBlue.copy(alpha = 0.3f)
        )
    )
}

