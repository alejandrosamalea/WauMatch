package com.example.waumatch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.waumatch.viewmodel.ChatViewModel
import com.example.waumatch.viewmodel.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class MessageWithId(
    val id: String,
    val message: Message
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    userId: String,
    viewModel: ChatViewModel
) {
    val currentUser = viewModel.currentUser
    val messages = remember { mutableStateListOf<MessageWithId>() }
    var messageText by remember { mutableStateOf("") }

    var userName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("nombre") ?: "Usuario"
                } else {
                    userName = "Usuario"
                }
            }
            .addOnFailureListener {
                userName = "Usuario"
            }
    }

    LaunchedEffect(userId) {
        val chat = viewModel.getChatWithUser(userId)
        val chatId = chat?.id

        if (chatId != null) {
            FirebaseFirestore.getInstance()
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        messages.clear()
                        for (doc in snapshot.documents) {
                            val message = doc.toObject(Message::class.java)
                            if (message != null) {
                                messages.add(MessageWithId(doc.id, message))
                            }
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversación con ${userName ?: "Cargando..."}") },
                actions = {
                    IconButton(onClick = {

                        navController.navigate("chat") {
                            popUpTo("chat") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { messageWithId ->
                    val message = messageWithId.message
                    val isMe = message.senderId == currentUser.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isMe) Color(0xFF4CAF50) else Color(0xFF2196F3),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 2.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = message.content,
                                modifier = Modifier.padding(12.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje") }
                )
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(userId, messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}