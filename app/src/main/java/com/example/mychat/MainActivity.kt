package com.example.mychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mychat.ui.chat.ChatScreen
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
                    // Launch the encrypted chat interface
                    // In a multi-screen app, you would pass these IDs via NavHost arguments
                    ChatScreen(
                        conversationId = "local_peer_session_1",
                        currentUserId = "user_me"
                    )
                }
            }
        }
    }
}
