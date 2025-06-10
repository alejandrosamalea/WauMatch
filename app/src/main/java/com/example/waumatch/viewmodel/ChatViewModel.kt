package com.example.waumatch.viewmodel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private var chatsListener: ListenerRegistration? = null
    private val messagesListeners = mutableMapOf<String, ListenerRegistration>()

    val currentUser: UserApp = getCurrentUserApp() ?: error("Usuario no autenticado")

    init {
        fetchChats()
    }

    private fun sendNotificationToUser(receiverId: String, senderName: String, messageContent: String, chatId: String) {
        val client = OkHttpClient()
        val onesignalAppId = "038e24e7-eca7-426a-868c-f513079bb67c" // Reemplaza con tu App ID
        val onesignalApiKey = "os_v2_app_aohcjz7mu5bgvbum6ujqpg5wpqlexcqsdhaetqfr7knivg2apiqvd3noutm2smqy43ca72e2z3f5vb543ag4gru3mudccsdtjcrn2vy" // Reemplaza con tu REST API Key

        val json = """
        {
            "app_id": "$onesignalAppId",
            "include_external_user_ids": ["$receiverId"],
            "contents": {"en": "$senderName: $messageContent"},
            "headings": {"en": "Nuevo mensaje en WauMatch"},
            "data": {"chatId": "$chatId"}
        }
    """.trimIndent()

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json
        )

        val request = Request.Builder()
            .url("https://onesignal.com/api/v1/notifications")
            .post(body)
            .addHeader("Authorization", "Basic $onesignalApiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("Notificación enviada con éxito")
                } else {
                    println("Error al enviar notificación: ${response.message}")
                }
            } catch (e: IOException) {
                println("Error al enviar notificación: ${e.message}")
            }
        }
    }

    private fun fetchChats() {
        chatsListener?.remove()
        chatsListener = db.collection("chats")
            .whereArrayContains("participants", currentUser.id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val chatsList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(id = doc.id)
                    }

                    chatsList.forEach { chat ->
                        listenMessages(chat.id ?: return@forEach)
                    }
                    _chats.value = chatsList
                }
            }
    }

    private fun listenMessages(chatId: String) {
        messagesListeners[chatId]?.remove()
        messagesListeners[chatId] = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val messagesList = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    val updatedChats = _chats.value.map { chat ->
                        if (chat.id == chatId) {
                            chat.copy(messages = messagesList.toMutableList())
                        } else chat
                    }
                    _chats.value = updatedChats

                    // 🔥 Si estás viendo este chat, marca mensajes como leídos
                    if (_chatIdWithUser.value == chatId) {
                        markMessagesAsRead(chatId)
                    }
                }
            }
    }
    fun markMessagesAsRead(chatId: String) {
        val currentUserId = currentUser.id
        val messagesRef = db.collection("chats").document(chatId).collection("messages")

        messagesRef
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("leido", false)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    messagesRef.document(doc.id).update("leido", true)
                }
            }
            .addOnFailureListener {
                println("Error marcando mensajes como leídos: ${it.message}")
            }
    }

    fun sendMessage(toUserId: String, content: String, onChatReady: (String) -> Unit = {}) {
        val chat = _chats.value.find { it.participants.contains(toUserId) && it.participants.contains(currentUser.id) }

        val message = Message(
            senderId = currentUser.id,
            receiverId = toUserId,
            content = content,
            timestamp = Timestamp.now()
        )

        // Obtén el nombre del remitente
        val senderName = currentUser.name ?: "Usuario"

        if (chat == null) {
            val newChat = Chat(
                participants = listOf(currentUser.id, toUserId),
                messages = mutableListOf(message)
            )
            db.collection("chats").add(newChat)
                .addOnSuccessListener { documentRef ->
                    val chatId = documentRef.id
                    _chats.value = _chats.value + newChat.copy(id = chatId)
                    documentRef.collection("messages")
                        .add(message)
                        .addOnSuccessListener {
                            sendNotificationToUser(toUserId, senderName, content, chatId)
                            onChatReady(chatId)
                        }
                        .addOnFailureListener { e ->
                            println("Error al guardar mensaje en nuevo chat: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    println("Error al crear nuevo chat: ${e.message}")
                }
        } else {
            chat.id?.let { chatId ->
                db.collection("chats").document(chatId).collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        sendNotificationToUser(toUserId, senderName, content, chatId)
                        onChatReady(chatId)
                    }
                    .addOnFailureListener { e ->
                        println("Error al guardar mensaje en chat existente: ${e.message}")
                    }
            } ?: println("Error: chat.id es null, no se puede guardar mensaje")
        }
    }

    private val _chatIdWithUser = MutableStateFlow<String?>(null)
    val chatIdWithUser: StateFlow<String?> = _chatIdWithUser

    fun loadChatWithUser(userId: String) {
        viewModelScope.launch {
            val localChat = _chats.value.find {
                it.participants.contains(userId) && it.participants.contains(currentUser.id)
            }

            if (localChat != null) {
                _chatIdWithUser.value = localChat.id
                println("✅ Chat encontrado localmente: ${localChat.id}")
            } else {
                // 🔥 Buscar en Firestore directamente
                FirebaseFirestore.getInstance()
                    .collection("chats")
                    .whereArrayContains("participants", currentUser.id)
                    .get()
                    .addOnSuccessListener { result ->
                        val chat = result.documents
                            .mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id) }
                            .find { it.participants.contains(userId) }

                        if (chat != null) {
                            _chatIdWithUser.value = chat.id
                            println("🌐 Chat encontrado en Firestore: ${chat.id}")
                        } else {
                            println("❌ No se encontró chat con $userId")
                            _chatIdWithUser.value = null
                        }
                    }
                    .addOnFailureListener {
                        println("🔥 Error al buscar chat en Firestore: ${it.message}")
                        _chatIdWithUser.value = null
                    }
            }
        }
    }
    fun getChatWithUser(userId: String): Chat? {
        return _chats.value.find {
            it.participants.contains(userId) && it.participants.contains(currentUser.id)
        }
    }


    override fun onCleared() {
        super.onCleared()
        chatsListener?.remove()
        messagesListeners.values.forEach { it.remove() }
    }
}
