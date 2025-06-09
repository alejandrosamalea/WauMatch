package com.example.waumatch.viewmodel
import com.google.firebase.Timestamp
data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val leido: Boolean = false
)
