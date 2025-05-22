package com.example.waumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.waumatch.viewmodel.Chat
import com.example.waumatch.viewmodel.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ChatItem(chat: Chat, currentUserId: String, onClick: (String) -> Unit) {
    val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: "Desconocido"
    var otherUserName by remember { mutableStateOf<String?>(null) }

    // Obtener el nombre del usuario por su ID
    LaunchedEffect(otherUserId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    otherUserName = document.getString("nombre") ?: otherUserId
                } else {
                    otherUserName = otherUserId
                }
            }
            .addOnFailureListener {
                otherUserName = otherUserId
            }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(80.dp)
            .clickable { onClick(otherUserId) },
        color = Color(0xFFBBDEFB), // Azul claro
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = otherUserName ?: "Cargando...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel) {
    val chatList by viewModel.chats.collectAsState()
    val currentUserId = viewModel.currentUser.id

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF024873), Color(0xFF1D7A93))
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                "Tus chats",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            chatList.forEach { chat ->
                ChatItem(chat = chat, currentUserId = currentUserId) { otherUserId ->
                    navController.navigate("chatDetail/$otherUserId")
                }
            }
        }
    }
}

