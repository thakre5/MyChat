// Path: app/src/main/java/com/example/mychat/ui/chat/ChatViewModel.kt
package com.example.mychat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.crypto.CryptoManager
import com.example.mychat.data.local.AppDatabase
import com.example.mychat.data.repository.MessageRepository
import com.example.mychat.p2p.WebRtcManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository
    val cryptoManager: CryptoManager
    private var webRtcManager: WebRtcManager? = null

    // UI States for pairing and connection
    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _localSdpOffer = MutableStateFlow("")
    val localSdpOffer: StateFlow<String> = _localSdpOffer.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MessageRepository(database.messageDao())
        cryptoManager = CryptoManager(application)
    }

    /**
     * Step 1: Called when you scan your peer's public key QR code.
     */
    fun onPeerKeyScanned(peerPublicKeyBase64: String, conversationId: String) {
        viewModelScope.launch {
            _connectionStatus.value = "Peer Key Scanned. Initializing WebRTC..."
            
            // Initialize WebRTC Manager for this session
            webRtcManager = WebRtcManager(
                context = getApplication(),
                onMessageReceived = { incomingCipherText ->
                    handleIncomingMessage(conversationId, incomingCipherText)
                },
                onConnectionStateChanged = { state ->
                    _connectionStatus.value = "Connection State: $state"
                }
            )

            // Step 2: Automatically generate a WebRTC connection offer 
            // to send back to the peer
            webRtcManager?.createOffer { sdpOffer ->
                _localSdpOffer.value = sdpOffer
                _connectionStatus.value = "Ready: Share Offer SDP with Peer"
            }
        }
    }

    private fun handleIncomingMessage(conversationId: String, cipherText: String) {
        viewModelScope.launch {
            try {
                // Decrypt incoming message using device private key
                val plaintext = cryptoManager.decryptIncomingMessage(cipherText)
                repository.saveMessage(conversationId, "peer_user", plaintext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendSecureMessage(conversationId: String, currentUserId: String, peerPublicKeyBase64: String, plaintext: String) {
        viewModelScope.launch {
            try {
                // Encrypt with peer's public key via Google Tink (Curve25519)
                val cipherText = cryptoManager.encryptMessageForPeer(peerPublicKeyBase64, plaintext)
                
                // Transmit over WebRTC DataChannel
                webRtcManager?.sendDirectMessage(cipherText)

                // Save locally
                repository.saveMessage(conversationId, currentUserId, plaintext)
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
