// Path: app/src/main/java/com/example/mychat/MainActivity.kt
package com.example.mychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mychat.ui.chat.ChatViewModel
import com.example.mychat.ui.pairing.PairingScreen
import com.example.mychat.ui.theme.MyChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ChatViewModel = viewModel()
                    
                    // Generate this device's public key for the QR code
                    val myPublicKey = viewModel.cryptoManager.getMyPublicKeyBase64()

                    PairingScreen(
                        myPublicKeyBase64 = myPublicKey,
                        onPeerKeyScanned = { scannedKey ->
                            // Trigger pairing and WebRTC offer creation when peer's QR is scanned
                            viewModel.onPeerKeyScanned(
                                peerPublicKeyBase64 = scannedKey,
                                conversationId = "mychat_session_1"
                            )
                        }
                    )
                }
            }
        }
    }
}
