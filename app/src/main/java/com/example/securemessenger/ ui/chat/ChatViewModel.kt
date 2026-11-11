package com.example.securemessenger.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.securemessenger.data.local.AppDatabase
import com.example.securemessenger.data.local.MessageEntity
import com.example.securemessenger.data.repository.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MessageRepository(database.messageDao())
    }

    // Expose messages for a specific active conversation ID
    fun getMessagesForChat(conversationId: String): StateFlow<List<MessageEntity>> {
        return repository.getMessages(conversationId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // Handle sending a new message (encrypting/saving locally)
    fun sendEncryptedMessage(conversationId: String, senderId: String, cipherText: String) {
        viewModelScope.launch {
            repository.sendMessage(conversationId, senderId, cipherText)
        }
    }
}
