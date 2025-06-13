package com.example.waumatch.auth

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.theme.WauMatchTheme
import com.google.android.play.core.integrity.r
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val showResendLink = remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

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
                    text = "Cuidamos a tu mejor amigo",
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

                // Mensaje de error
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

// ⬇️ Aquí colocas esto:
                if (showResendLink.value) {
                    Text(
                        text = "Reenviar correo de verificación",
                        color = Color(0xFF2EDFF2),
                        style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline),
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable {
                                resendVerificationEmail(auth) { error ->
                                    errorMessage = error
                                    showResendLink.value = false
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ¿Olvidaste tu contraseña?
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 14.sp,
                    color = Color(0xFF1EB7D9),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { navController.navigate(NavigationItem.Recuperar.route) }
                        .padding(bottom = 24.dp)
                )

                // Botón de inicio de sesión
                Button(
                    onClick = {
                        validate(
                            email.lowercase(Locale.getDefault()),
                            password,
                            auth,
                            setErrorMessage = { errorMessage = it },
                            setShowResendLink = { showResendLink.value = it },
                            onValidationComplete = { isValid ->
                                if (isValid) {
                                    navController.navigate("home")
                                }
                            }
                        )
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
                        "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Link de registro
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = "Regístrate",
                        fontSize = 14.sp,
                        color = Color(0xFF2EDFF2),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(NavigationItem.Registrar.route)
                        }
                    )
                }
            }
        }
    }
}

fun resendVerificationEmail(auth: FirebaseAuth, setErrorMessage: (String) -> Unit) {
    val user = auth.currentUser
    if (user != null && !user.isEmailVerified) {
        FirebaseAuth.getInstance().setLanguageCode("es")
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setErrorMessage("Correo de verificación reenviado.")
                } else {
                    setErrorMessage("Error al reenviar el correo. Inténtalo más tarde.")
                }
            }
    } else {
        setErrorMessage("No se puede reenviar el correo. Inicia sesión nuevamente.")
    }
}
/*
fun validate(email: String, password: String, auth: FirebaseAuth, setErrorMessage: (String) -> Unit, onValidationComplete: (Boolean) -> Unit) {
    if (email.isEmpty() || password.isEmpty()) {
        setErrorMessage("Todos los campos son obligatorios")
        onValidationComplete(false)
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onValidationComplete(true)
            } else {
                setErrorMessage("Contraseña o email incorrectos")
                onValidationComplete(false)
            }
        }
}*/

fun validate(

    email: String,
    password: String,
    auth: FirebaseAuth,
    setErrorMessage: (String) -> Unit,
    setShowResendLink: (Boolean) -> Unit,
    onValidationComplete: (Boolean) -> Unit
) {
    val correosExentos = listOf(
        "javier@gmail.com",
        "javier1@gmail.com",
        "alejandro@gmail.com",
        "alejandro1@gmail.com",
        "ivan@gmail.com",
        "ivan1@gmail.com",
        "Javier@gmail.com",
        "Javier1@gmail.com",
        "Alejandro@gmail.com",
        "Alejandro1@gmail.com",
        "Ivan@gmail.com",
        "Ivan1@gmail.com"
    )
    if (email.isEmpty() || password.isEmpty()) {
        setErrorMessage("Todos los campos son obligatorios")
        setShowResendLink(false)
        onValidationComplete(false)
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && (user.isEmailVerified || email in correosExentos)) {
                    onValidationComplete(true)
                } else {
                    // Cambia el idioma del correo de verificación a español
                    FirebaseAuth.getInstance().setLanguageCode("es")

                    // Enviar correo de verificación
                    user?.sendEmailVerification()

                    // Mensaje de error y opción de reenviar
                    setErrorMessage("Verifica tu correo electrónico. Se ha enviado un nuevo enlace.")
                    setShowResendLink(true)

                    auth.signOut()
                    onValidationComplete(false)
                }
            } else {
                setErrorMessage("Contraseña o email incorrectos")
                setShowResendLink(false)
                onValidationComplete(false)
            }
        }
}