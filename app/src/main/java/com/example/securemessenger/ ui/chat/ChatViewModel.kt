// Path: app/src/main/java/com/example/securemessenger/ui/chat/ChatViewModel.kt
package com.example.securemessenger.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.securemessenger.crypto.CryptoManager
import com.example.securemessenger.data.local.AppDatabase
import com.example.securemessenger.data.repository.MessageRepository
import com.example.securemessenger.p2p.WebRtcManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository
    private val cryptoManager: CryptoManager
    private var webRtcManager: WebRtcManager? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MessageRepository(database.messageDao())
        
        // Initialize cryptographic keys (Curve25519 via Tink)
        cryptoManager = CryptoManager(application)
    }

    /**
     * Initializes the P2P WebRTC transport and binds incoming messages to decryption & database storage.
     */
    fun initializeP2P(conversationId: String, peerPublicKeyBase64: String) {
        webRtcManager = WebRtcManager(
            context = getApplication(),
            onMessageReceived = { incomingCipherText ->
                viewModelScope.launch {
                    try {
                        // 1. Decrypt incoming payload using device private key
                        val decryptedPlaintext = cryptoManager.decryptIncomingMessage(incomingCipherText)
                        
                        // 2. Save plaintext into encrypted Room/SQLCipher vault
                        repository.saveMessage(
                            conversationId = conversationId,
                            senderId = "peer_user",
                            textContent = decryptedPlaintext
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onConnectionStateChanged = { state ->
                // Handle connection established or dropped states here
            }
        )
    }

    fun getMessagesForChat(conversationId: String): StateFlow<List<MessageEntity>> {
        return repository.getMessages(conversationId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Sends a message: Encrypts with peer's public key, transmits over WebRTC, and saves locally.
     */
    fun sendSecureMessage(conversationId: String, currentUserId: String, peerPublicKeyBase64: String, plaintext: String) {
        viewModelScope.launch {
            try {
                // 1. Encrypt plaintext for the specific peer using Google Tink (Curve25519)
                val cipherText = cryptoManager.encryptMessageForPeer(peerPublicKeyBase64, plaintext)

                // 2. Transmit ciphertext directly over WebRTC P2P DataChannel
                webRtcManager?.sendDirectMessage(cipherText)

                // 3. Persist locally to Room + SQLCipher database for local history
                repository.saveMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    textContent = plaintext // Stored locally decrypted for user convenience
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webRtcManager?.close()
    }
}
