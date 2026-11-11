package com.example.mychat.ui.pairing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.default.QrCode
import androidx.compose.material.icons.default.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mychat.crypto.QrCodeGenerator
import com.example.mychat.ui.qr.QrScannerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    myPublicKeyBase64: String,
    onPeerKeyScanned: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My QR Code", "Scan Peer QR")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("P2P Secure Pairing") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector to switch between Showing and Scanning QR codes
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.QrCode else Icons.Default.QrCodeScanner,
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            // Display content based on active tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    0 -> DisplayMyQrTab(myPublicKeyBase64 = myPublicKeyBase64)
                    1 -> ScanPeerQrTab(onKeyScanned = onPeerKeyScanned)
                }
            }
        }
    }
}

@Composable
fun DisplayMyQrTab(myPublicKeyBase64: String) {
    val qrBitmap = remember(myPublicKeyBase64) {
        if (myPublicKeyBase64.isNotBlank()) {
            QrCodeGenerator.generateQrBitmap(myPublicKeyBase64)
        } else {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Share Your Public Key",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Have your peer scan this QR code to establish an encrypted P2P connection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = "My Public Key QR Code",
                    modifier = Modifier.size(240.dp)
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ScanPeerQrTab(onKeyScanned: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scan Peer's QR Code",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Embed the CameraX + ML Kit Scanner screen inside the tab container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            QrScannerScreen(onKeyScanned = onKeyScanned)
        }
    }
}
