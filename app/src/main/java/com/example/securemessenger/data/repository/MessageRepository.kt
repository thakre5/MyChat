// Path: app/src/main/java/com/example/securemessenger/data/repository/MessageRepository.kt
package com.example.securemessenger.data.repository

import com.example.securemessenger.data.local.MessageDao
import com.example.securemessenger.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun saveMessage(conversationId: String, senderId: String, textContent: String) {
        val message = MessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            cipherTextContent = textContent,
            timestamp = System.currentTimeMillis(),
            isDelivered = true
        )
        messageDao.insertMessage(message)
    }
}
