package com.example.securemessenger.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(
    conversationId: String,
    currentUserId: String,
    viewModel: ChatViewModel = viewModel()
) {
    // Observe the encrypted database flow safely as Compose State
    val messages by viewModel.getMessagesForChat(conversationId).collectAsState()
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Message list container
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.senderId == currentUserId)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Displaying decrypted content fetched live from SQLCipher
                        Text(text = message.cipherTextContent, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row for composing new messages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type an encrypted message...") }
            )
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendEncryptedMessage(conversationId, currentUserId, textInput)
                        textInput = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}
