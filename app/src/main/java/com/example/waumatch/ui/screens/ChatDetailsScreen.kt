package com.example.waumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.waumatch.viewmodel.ChatViewModel
import com.example.waumatch.viewmodel.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration

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
    var messagesListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    val chatId by viewModel.chatIdWithUser.collectAsState()

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                userName = document.getString("nombre") ?: "Usuario"
            }
            .addOnFailureListener {
                userName = "Usuario"
            }
    }

    LaunchedEffect(userId) {
        viewModel.loadChatWithUser(userId)
    }

    fun subscribeToMessages(chatId: String) {
        messagesListener?.remove()
        messagesListener = db.collection("chats")
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

    LaunchedEffect(chatId) {
        if (chatId != null) {
            subscribeToMessages(chatId!!)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            messagesListener?.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversación con ${userName ?: "Cargando..."}") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("chat") {
                            popUpTo("chat") { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF024873), Color(0xFF1D7A93))
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(messages) { index, messageWithId ->
                        val message = messageWithId.message
                        val isMe = message.senderId == currentUser.id

                        val time = remember(message.timestamp) {
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            message.timestamp?.toDate()?.let { sdf.format(it) } ?: ""
                        }

                        val date = remember(message.timestamp) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            message.timestamp?.toDate()?.let { sdf.format(it) } ?: ""
                        }

                        val previousDate = if (index > 0) {
                            messages[index - 1].message.timestamp?.toDate()?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            }
                        } else null

                        Column {
                            // Fecha si es el primer mensaje del día
                            if (date != previousDate) {
                                Text(
                                    text = date,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                val configuration = LocalConfiguration.current
                                val maxWidth = (configuration.screenWidthDp * 0.7).dp

                                Surface(
                                    color = if (isMe) Color(0xFF1D7A93) else Color(0xFF024873),
                                    shape = MaterialTheme.shapes.medium,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = MaterialTheme.shapes.medium,
                                            clip = false
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = maxWidth)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text(
                                                text = message.content,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .weight(1f, fill = false)
                                                    .padding(end = 8.dp)
                                            )
                                            Text(
                                                text = time,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text("Escribe un mensaje")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .shadow(4.dp, shape = RoundedCornerShape(20.dp)),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFF2A4F63),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(userId, messageText) { newChatId ->
                                    subscribeToMessages(newChatId)
                                }
                                messageText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .shadow(6.dp, shape = RoundedCornerShape(16.dp))
                    ) {
                        Text("Enviar", color = Color.White)
                    }
                }
            }
        }
    }
}