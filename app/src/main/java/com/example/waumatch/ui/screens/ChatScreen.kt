package com.example.waumatch.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(otherUserId) }
            .padding(vertical = 8.dp)
    ) {
        Text(text = otherUserName ?: "Cargando...", style = MaterialTheme.typography.bodyLarge)
        if (chat.messages.isNotEmpty()) {
            Text(
                text = chat.messages.last().content,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel) {
    val chatList by viewModel.chats.collectAsState()
    val currentUserId = viewModel.currentUser.id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tus chats", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        chatList.forEach { chat ->
            ChatItem(chat = chat, currentUserId = currentUserId) { otherUserId ->
                navController.navigate("chatDetail/$otherUserId")
            }
        }
    }
}
