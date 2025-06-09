package com.example.waumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.waumatch.viewmodel.Chat
import com.example.waumatch.viewmodel.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ChatItem(
    otherUserName: String,
    lastMessage: String,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(IntrinsicSize.Min)
            .clickable { onClick() },
        color = Color(0xFFBBDEFB),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp), // aumenté aquí el vertical de 8 a 16
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = otherUserName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel) {
    val chatList by viewModel.chats.collectAsState()
    val currentUserId = viewModel.currentUser.id
    val nombres = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(chatList) {
        val db = FirebaseFirestore.getInstance()
        chatList.forEach { chat ->
            val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return@forEach
            if (!nombres.containsKey(otherUserId)) {
                db.collection("usuarios")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener { document ->
                        val nombre = document.getString("nombre") ?: "Desconocido"
                        nombres[otherUserId] = nombre
                    }
                    .addOnFailureListener {
                        nombres[otherUserId] = "Desconocido"
                    }
            }
        }
    }

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

            chatList
                .sortedByDescending { it.messages.maxByOrNull { msg -> msg.timestamp }?.timestamp }
                .forEach { chat ->
                    val otherUserId = chat.participants.firstOrNull { it != currentUserId } ?: return@forEach
                    val otherUserName = nombres[otherUserId] ?: "Cargando..."
                    val lastMessage = chat.messages.maxByOrNull { it.timestamp }?.content ?: ""
                    val unreadCount = chat.messages.count {
                        it.receiverId == currentUserId && !it.leido
                    }

                    ChatItem(
                        otherUserName = otherUserName,
                        lastMessage = lastMessage,
                        unreadCount = unreadCount
                    ) {
                        navController.navigate("chatDetail/$otherUserId")
                    }
                }
        }
    }
}
