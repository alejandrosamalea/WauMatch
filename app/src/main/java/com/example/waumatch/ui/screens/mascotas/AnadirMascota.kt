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
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.waumatch.data.MascotaRepository
import com.example.waumatch.ui.components.Mascota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnadirMascota(navController: NavController) {
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
    val imageUris = remember { mutableStateListOf<String?>(null, null, null) }
    val pickImageLaunchers = List(3) { index ->
        rememberLauncherForActivityResult(

            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                imageUris[index] = it.toString()
            }
        }
    }
    var adicional by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Agregar Mascota") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ExpandableSection("Información básica") {
                TextField(nombre, { nombre = it }, label = "Nombre")
                DropdownSelector(
                    label = "Especie",
                    options = listOf("Perro", "Gato", "Conejo", "Ave", "Hámster"),
                    selectedOption = especie,
                    onOptionSelected = { especie = it }
                )
                TextField(raza, { raza = it }, label = "Raza")
                TextField(edad, { edad = it }, label = "Edad", keyboardType = KeyboardType.Number)
            }

            ExpandableSection("Imágenes de la mascota") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) { index ->
                        Log.w("INFO ADMIN","os odio")
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
                    if (nombre.isBlank() || especie.isBlank() || raza.isBlank() || edad.isBlank()) {
                        Log.e("Validación", "Faltan campos obligatorios")
                        return@Button
                    }
                    val mascotaId = FirebaseFirestore.getInstance().collection("mascotas").document(userId).collection("lista").document().id
                    val mascota = Mascota( mascotaId,
                        nombre, especie, raza, edad.toIntOrNull() ?: 0, imageUris.filterNotNull(), horariosComida, tipoComida,
                        ritualesComida, lugarDormir, horarioDormir, habitosHigiene, frecuenciaPaseo, limpieza,
                        productosEspeciales, juguetesFavoritos, medicacion, veterinario, restricciones,
                        accionesNoToleradas, puedeQuedarseSolo, ansiedadSeparacion, escapa,
                        habitacionesRestringidas, adicional, userId, telefonoDuenio, correoDuenio, contactoAlternativo
                    )
                    repository.agregarMascota(mascota,
                        onSuccess = { navController.navigate("AdminMascota") },
                        onError = { Log.e("AddPet", "Error", it) }
                    )
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Guardar Mascota")
            }
        }
    }
}

@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (expanded) 90f else 270f)
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(text = { Text(selectionOption) }, onClick = {
                    onOptionSelected(selectionOption)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType)
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
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType
        ),
        maxLines = Int.MAX_VALUE,
        singleLine = false
    )
}

