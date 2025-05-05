package com.example.waumatch.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.components.WauMatchHeader
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun AddScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))

    var showDialog by remember { mutableStateOf(true) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var creador by remember { mutableStateOf("") }

    // Variables para el DatePicker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            // Formateamos la fecha seleccionada
            val formattedDate = "$dayOfMonth/${month + 1}/$year"
            // Actualizamos las fechas de inicio o fin
            if (fechaInicio.isEmpty()) fechaInicio = formattedDate
            else fechaFin = formattedDate
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    if (userId.isNotBlank()) {
        // Llamada a Firestore para obtener el nombre del usuario
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("usuarios").document(userId)

        LaunchedEffect(userId) {
            try {
                // Obtén los datos del usuario desde Firestore
                val userDocument = userRef.get().await()
                val nombreUsuario = userDocument.getString("nombre") ?: "Usuario Anónimo"
                creador = nombreUsuario
            } catch (e: Exception) {
                // Manejo de errores en caso de que no se pueda obtener el nombre
                creador = "Usuario Anónimo"
            }
        }

        if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nuevo Anuncio") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha inicio") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaFin,
                        onValueChange = { fechaFin = it },
                        label = { Text("Fecha fin") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = creador,
                        onValueChange = {},
                        label = { Text("Creador") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titulo.isNotBlank() && descripcion.isNotBlank() && fechaInicio.isNotBlank() && fechaFin.isNotBlank()) {
                            val nuevoAnuncio = AnuncioEntity(
                                id = UUID.randomUUID().toString(),
                                titulo = titulo,
                                descripcion = descripcion,
                                fechaInicio = fechaInicio,
                                fechaFin = fechaFin,
                                creador = creador,  // Añadido el creador
                                esFavorito = false
                            )
                            viewModel.agregarAnuncio(nuevoAnuncio)
                            Toast.makeText(context, "Anuncio creado", Toast.LENGTH_SHORT).show()
                            showDialog = false
                        } else {
                            Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Publicar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    } else {
        val anuncios by viewModel.anuncios.collectAsState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            WauMatchHeader()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(anuncios) { anuncio ->
                    AnuncioCard(
                        anuncio = anuncio,
                        isExpanded = false,
                        onClick = {},
                        onClose = {},
                        onToggleFavorito = { viewModel.toggleFavorito(it) })
                }
            }
        }
    }
}
}
