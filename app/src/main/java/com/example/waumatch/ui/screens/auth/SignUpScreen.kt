package com.example.waumatch.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.test.services.events.TimeStamp
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.theme.WauMatchTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()
    var provinciaSeleccionada by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val provinciasEspaña = listOf(
        "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz",
        "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real",
        "Córdoba", "La Coruña", "Cuenca", "Gerona", "Granada", "Guadalajara", "Guipúzcoa",
        "Huelva", "Huesca", "Islas Baleares", "Jaén", "León", "Lérida", "Lugo", "Madrid",
        "Málaga", "Murcia", "Navarra", "Orense", "Palencia", "Las Palmas", "Pontevedra",
        "La Rioja", "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria",
        "Tarragona", "Teruel", "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
    )

    WauMatchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF111826),
                            Color(0xFF022859)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Image(
                    painter = rememberAsyncImagePainter(
                        R.drawable.perro
                    ),
                    contentDescription = "Logo de WauMatch",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = "WauMatch",
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color(0xFF2EDFF2),
                    textAlign = TextAlign.Center
                )

                // Subtítulo
                Text(
                    text = "Crea tu cuenta para empezar",
                    fontSize = 16.sp,
                    color = Color(0xFF1EB7D9),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // Campo de correo
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo electrónico", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF2EDFF2)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Nombre de usuario", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_account_box_24),
                            contentDescription = null,
                            tint = Color(0xFF2EDFF2)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = provinciaSeleccionada,
                        onValueChange = { provinciaSeleccionada = it },
                        readOnly = true,
                        label = { Text("Provincia", color = Color(0xFF6B7280)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0x14FFFFFF),
                            unfocusedContainerColor = Color(0x14FFFFFF),
                            focusedBorderColor = Color(0x1AFFFFFF),
                            unfocusedBorderColor = Color(0x1AFFFFFF),
                            focusedLabelColor = Color(0xFF6B7280),
                            unfocusedLabelColor = Color(0xFF6B7280)
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        provinciasEspaña.forEach { provincia ->
                            DropdownMenuItem(
                                text = { Text(provincia) },
                                onClick = {
                                    provinciaSeleccionada = provincia
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF2EDFF2)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                painter = painterResource(id = if (showPassword) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24),
                                contentDescription = null,
                                tint = Color(0xFF2EDFF2)
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de confirmación de contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirmar contraseña", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF2EDFF2)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                painter = painterResource(id = if (showConfirmPassword) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24),
                                contentDescription = null,
                                tint = Color(0xFF2EDFF2)
                            )
                        }
                    },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_error_24),
                            contentDescription = null,
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validate(email, password, confirmPassword, nombre, auth, { errorMessage = it })) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        FirebaseAuth.getInstance().setLanguageCode("es")
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    crearUsuarioBD(db, email, password, nombre, direccion, auth, telefono, provinciaSeleccionada)
                                                    navController.navigate(NavigationItem.Login.route)
                                                } else {
                                                    errorMessage = "Error al enviar el correo de verificación: ${verifyTask.exception?.message}"
                                                }
                                            }
                                    } else {
                                        errorMessage = task.exception?.message ?: "Ha ocurrido un error al intentar crear la cuenta"
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2EDFF2),
                        contentColor = Color(0xFF111826)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Crear Cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "Inicia sesión",
                        fontSize = 14.sp,
                        color = Color(0xFF2EDFF2),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(NavigationItem.Login.route)
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
fun crearUsuarioBD(bd: FirebaseFirestore, email: String, password: String, nombre: String, direccion: String, auth: FirebaseAuth, telefono: String, provincia: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val imageUrl = generateProfileImageFromName(nombre)
    user?.let {
        val usuarioData = hashMapOf(
            "nombre" to nombre,
            "email" to email,
            "direccion" to direccion,
            "telefono" to telefono,
            "profileImage" to imageUrl,
            "provincia" to provincia,
            "fechaRegistro" to SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date()),
            "profileImage" to imageUrl
        )
        bd.collection("usuarios").document(user.uid).set(usuarioData)
            .addOnSuccessListener {
                Log.d("Firestore", "Usuario creado correctamente en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al guardar el usuario: ${e.message}")
            }
    }
}


fun validate(email: String, password: String, confirmPassword: String, nombre: String, auth: FirebaseAuth, setErrorMessage: (String) -> Unit): Boolean {
    val regex = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"

    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty()) {
        setErrorMessage("Todos los campos son obligatorios")
        return false
    }

    if(!(email.matches(regex.toRegex())))
    {
        setErrorMessage("Por favor, ingresa un email válido con un dominio permitido (gmail.com, hotmail.com, etc.)")
        return false
    }

    if (password != confirmPassword) {
        setErrorMessage("Las contraseñas no coinciden")
        return false
    }
    var isValid = true

    return isValid
}
fun generateProfileImageFromName(name: String): String {
    val firstLetter = name.trim().firstOrNull()?.uppercase() ?: "U"
    val backgroundColor = "022859"
    val textColor = "FFFFFF"
    return "https://ui-avatars.com/api/?name=$firstLetter&background=$backgroundColor&color=$textColor"
}