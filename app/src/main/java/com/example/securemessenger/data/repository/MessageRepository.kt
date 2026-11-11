package com.example.securemessenger.data.repository

import com.example.securemessenger.data.local.MessageDao
import com.example.securemessenger.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {

    // Exposes the encrypted database stream as a reactive Flow
    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun sendMessage(conversationId: String, senderId: String, cipherText: String) {
        val message = MessageEntity(
            conversationId = conversationId,
            senderId = senderId,
            cipherTextContent = cipherText,
            timestamp = System.currentTimeMillis(),
            isDelivered = true
        )
        messageDao.insertMessage(message)
    }
}
