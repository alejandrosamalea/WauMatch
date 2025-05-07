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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.R

import com.example.waumatch.ui.navigation.NavigationItem
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RecuperarScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

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
                painter = rememberAsyncImagePainter(R.drawable.perro),
                contentDescription = "Logo de WauMatch",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Título
            Text(
                text = "Recuperar Contraseña",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF2EDFF2),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            // Mensaje
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = if (isError) Color(0xFFFF6B6B) else Color(0xFF2ECC71), // rojo o verde
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de enviar
            Button(
                onClick = {
                    sendPasswordReset(email, auth,
                        onSuccess = {
                            isError = false
                            message = it
                        },
                        onError = {
                            isError = true
                            message = it
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
                    "Enviar correo de recuperación",
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link para regresar
            Row(horizontalArrangement = Arrangement.Center) {

                Text(
                    text = "Regresar",
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

fun sendPasswordReset(
    email: String,
    auth: FirebaseAuth,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    if (email.isBlank()) {
        onError("Por favor, ingresa un correo válido.")
        return
    }
    FirebaseAuth.getInstance().setLanguageCode("es")
    auth.sendPasswordResetEmail(email)
        .addOnSuccessListener {
            onSuccess("Se ha enviado un correo a $email para recuperar tu contraseña.")
        }
        .addOnFailureListener {
            onError("No existe una cuenta asociada a este correo.")
        }
}
