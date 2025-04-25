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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.theme.WauMatchTheme
import com.google.android.play.core.integrity.r
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
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

                Spacer(modifier = Modifier.height(16.dp))

                // ¿Olvidaste tu contraseña?
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 14.sp,
                    color = Color(0xFF1EB7D9),
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { /* TODO: Implementar recuperación */ }
                        .padding(bottom = 24.dp)
                )

                // Botón de inicio de sesión
                Button(
                    onClick = {
                        validate(email, password, auth, { errorMessage = it }) { isValid ->
                            if (isValid) {
                                navController.navigate("home")

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
}