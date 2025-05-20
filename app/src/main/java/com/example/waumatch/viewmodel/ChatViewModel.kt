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
                }
            }
    }

    fun sendMessage(toUserId: String, content: String) {
        val chat = _chats.value.find { it.participants.contains(toUserId) }

        val message = Message(
            senderId = currentUser.id,
            receiverId = toUserId,
            content = content,
            timestamp = Timestamp.now()
        )

        if (chat == null) {
            val newChat = Chat(
                participants = listOf(currentUser.id, toUserId),
                messages = mutableListOf(message) // Agrega el mensaje que enviaste
            )
            db.collection("chats").add(newChat)
                .addOnSuccessListener { documentRef ->
                    val chatId = documentRef.id
                    // Actualiza localmente _chats agregando este nuevo chat con ID
                    _chats.value = _chats.value + newChat.copy(id = chatId)
                    // Luego guarda el mensaje en subcolección
                    documentRef.collection("messages")
                        .add(message)
                        .addOnSuccessListener { messageRef ->
                            println("Mensaje guardado en nuevo chat con ID de mensaje: ${messageRef.id}")
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
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener { messageRef ->
                        println("Mensaje guardado en chat existente con ID de mensaje: ${messageRef.id}")
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
