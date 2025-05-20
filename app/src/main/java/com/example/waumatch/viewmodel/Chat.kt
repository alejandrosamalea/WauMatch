package com.example.waumatch.viewmodel



data class Chat(
    val id: String? = null,
    val participants: List<String> = emptyList(),
    val messages: MutableList<Message> = mutableListOf()

)