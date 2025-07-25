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
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.theme.WauMatchTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()
    var provinciaSeleccionada by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val provinciasEspaña = mapOf(
        "Álava" to Pair(42.85, -2.68),
        "Albacete" to Pair(38.99, -1.86),
        "Alicante" to Pair(38.35, -0.48),
        "Almería" to Pair(36.84, -2.46),
        "Asturias" to Pair(43.36, -5.85),
        "Ávila" to Pair(40.66, -4.70),
        "Badajoz" to Pair(38.88, -6.97),
        "Barcelona" to Pair(41.39, 2.17),
        "Burgos" to Pair(42.34, -3.70),
        "Cáceres" to Pair(39.48, -6.37),
        "Cádiz" to Pair(36.53, -6.29),
        "Cantabria" to Pair(43.46, -3.80),
        "Castellón" to Pair(39.98, -0.03),
        "Ciudad Real" to Pair(38.98, -3.93),
        "Córdoba" to Pair(37.89, -4.78),
        "La Coruña" to Pair(43.36, -8.41),
        "Cuenca" to Pair(40.07, -2.14),
        "Gerona" to Pair(41.98, 2.82),
        "Granada" to Pair(37.18, -3.60),
        "Guadalajara" to Pair(40.63, -3.17),
        "Guipúzcoa" to Pair(43.32, -1.98),
        "Huelva" to Pair(37.27, -6.95),
        "Huesca" to Pair(42.14, -0.41),
        "Islas Baleares" to Pair(39.57, 2.65),
        "Jaén" to Pair(37.77, -3.79),
        "León" to Pair(42.60, -5.57),
        "Lérida" to Pair(41.62, 0.62),
        "Lugo" to Pair(43.01, -7.56),
        "Madrid" to Pair(40.42, -3.70),
        "Málaga" to Pair(36.72, -4.42),
        "Murcia" to Pair(37.98, -1.13),
        "Navarra" to Pair(42.82, -1.65),
        "Orense" to Pair(42.34, -7.86),
        "Palencia" to Pair(42.01, -4.53),
        "Las Palmas" to Pair(28.10, -15.42),
        "Pontevedra" to Pair(42.43, -8.64),
        "La Rioja" to Pair(42.46, -2.45),
        "Salamanca" to Pair(40.97, -5.66),
        "Santa Cruz de Tenerife" to Pair(28.47, -16.25),
        "Segovia" to Pair(40.95, -4.12),
        "Sevilla" to Pair(37.39, -5.99),
        "Soria" to Pair(41.77, -2.46),
        "Tarragona" to Pair(41.12, 1.25),
        "Teruel" to Pair(40.34, -1.11),
        "Toledo" to Pair(39.86, -4.02),
        "Valencia" to Pair(39.47, -0.38),
        "Valladolid" to Pair(41.65, -4.72),
        "Vizcaya" to Pair(43.26, -2.93),
        "Zamora" to Pair(41.50, -5.75),
        "Zaragoza" to Pair(41.65, -0.88)
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
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.perro),
                    contentDescription = "Logo de WauMatch",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "WauMatch",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2EDFF2),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Crea tu cuenta para empezar",
                    fontSize = 16.sp,
                    color = Color(0xFF1EB7D9),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        if (it.length <= 9 && it.all { char -> char.isDigit() }) {
                            telefono = it
                        }
                    },
                    placeholder = { Text("Teléfono (9 dígitos)", color = Color(0xFF6B7280)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF2EDFF2)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    modifier = Modifier.fillMaxWidth()
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
                        provinciasEspaña.keys.forEach { provincia ->
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2EDFF2),
                            uncheckedColor = Color(0xFF6B7280)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acepto los ",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "Términos y Condiciones",
                        fontSize = 14.sp,
                        color = Color(0xFF2EDFF2),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            navController.navigate(NavigationItem.TermsAndConditions.route)
                        }
                    )
                }

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
                        if (validate(email, password, confirmPassword, nombre, telefono, acceptTerms, auth, { errorMessage = it })) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        FirebaseAuth.getInstance().setLanguageCode("es")
                                        user?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    val coordenadas = provinciasEspaña[provinciaSeleccionada]
                                                    val latitud = coordenadas?.first
                                                    val longitud = coordenadas?.second
                                                    if (latitud != null && longitud != null) {
                                                        crearUsuarioBD(db,email.lowercase(Locale.getDefault()), password, nombre, auth, telefono, provinciaSeleccionada, latitud, longitud)
                                                    }
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
                        fontWeight = FontWeight.Bold,
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
fun crearUsuarioBD(bd: FirebaseFirestore, email: String, password: String, nombre: String, auth: FirebaseAuth, telefono: String, provincia: String, latitud: Double, longitud: Double) {
    val user = FirebaseAuth.getInstance().currentUser
    val radio_km = 100
    val imageUrl = generateProfileImageFromName(nombre)
    user?.let {
        val usuarioData = hashMapOf(
            "nombre" to nombre,
            "email" to email,
            "telefono" to telefono,
            "profileImage" to imageUrl,
            "provincia" to provincia,
            "latitud" to latitud,
            "longitud" to longitud,
            "radio_km" to radio_km,
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

fun validate(email: String, password: String, confirmPassword: String, nombre: String, telefono: String, acceptTerms: Boolean, auth: FirebaseAuth, setErrorMessage: (String) -> Unit): Boolean {
    val regex = "^[A-Za-z0-9._%+-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"
    if (!acceptTerms) {
        setErrorMessage("Debes aceptar los Términos y Condiciones para registrarte")
        return false
    }
    if (telefono.length != 9 || !telefono.all { it.isDigit() }) {
        setErrorMessage("El teléfono debe contener exactamente 9 dígitos")
        return false
    }
    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty()) {
        setErrorMessage("Todos los campos son obligatorios")
        return false
    }
    if (!email.matches(regex.toRegex())) {
        setErrorMessage("Por favor, ingresa un email válido con un dominio permitido (gmail.com, hotmail.com, etc.)")
        return false
    }
    if (password != confirmPassword) {
        setErrorMessage("Las contraseñas no coinciden")
        return false
    }
    val (isValidPassword, passwordError) = verificarPassword(password)
    if (!isValidPassword) {
        setErrorMessage(passwordError)
        return false
    }
    return true
}
fun verificarPassword(password: String): Pair<Boolean, String> {
    val minLength = 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }

    return when {
        password.length < minLength -> Pair(false, "La contraseña debe tener al menos 8 caracteres")
        !hasUpperCase -> Pair(false, "La contraseña debe contener al menos una letra mayúscula")
        !hasNumber -> Pair(false, "La contraseña debe contener al menos un número")
        else -> Pair(true, "")
    }
}
fun generateProfileImageFromName(name: String): String {
    val firstLetter = name.trim().firstOrNull()?.uppercase() ?: "U"
    val backgroundColor = "022859"
    val textColor = "FFFFFF"
    return "https://ui-avatars.com/api/?name=$firstLetter&background=$backgroundColor&color=$textColor"
}