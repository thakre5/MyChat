package com.example.securemessenger.crypto

import android.content.Context
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.cleartextkeyset.CleartextKeysetHandle
import com.google.crypto.tink.hybrid.HybridConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.ByteArrayOutputStream
import java.util.Base64

class CryptoManager(context: Context) {

    private val keysetHandle: KeysetHandle
    private val publicKeysetHandle: KeysetHandle

    init {
        // Initialize Tink's hybrid encryption primitives
        HybridConfig.register()

        // Securely manage the local private keyset using Android Keystore/MasterKey
        val masterKeyUri = "android-keystore://master_key_alias"
        val keysetFileName = "p2p_crypto_keyset.cfg"
        val preferenceFileName = "p2p_crypto_prefs"

        keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, keysetFileName, preferenceFileName)
            .withKeyTemplate(KeyTemplates.get("ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM")) // Or custom X25519 template
            .withMasterKeyUri(masterKeyUri)
            .build()
            .keysetHandle

        // Extract the public key handle to share with peers
        publicKeysetHandle = keysetHandle.publicKeysetHandle
    }

    /**
     * Exports this device's public key as a Base64 string to exchange with peers out-of-band (e.g., via QR code).
     */
    fun getMyPublicKeyBase64(): String {
        val outputStream = ByteArrayOutputStream()
        CleartextKeysetHandle.write(publicKeysetHandle, com.google.crypto.tink.JsonKeysetWriter.withOutputStream(outputStream))
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    /**
     * Encrypts a plaintext message using a peer's public key so only they can decrypt it.
     */
    fun encryptMessageForPeer(peerPublicKeyBase64: String, plaintextMessage: String): String {
        val decodedBytes = Base64.getDecoder().decode(peerPublicKeyBase64)
        val peerPublicKeysetHandle = CleartextKeysetHandle.read(
            com.google.crypto.tink.JsonKeysetReader.withInputStream(decodedBytes.inputStream())
        )

        val hybridEncrypt = peerPublicKeysetHandle.getPrimitive(HybridEncrypt::class.java)
        // Context info ensures binding to this specific P2P session context
        val ciphertextBytes = hybridEncrypt.encrypt(plaintextMessage.toByteArray(Charsets.UTF_8), "SecureP2PContext".toByteArray())
        
        return Base64.getEncoder().encodeToString(ciphertextBytes)
    }

    /**
     * Decrypts an incoming ciphertext message using this device's private key.
     */
    fun decryptIncomingMessage(base64Ciphertext: String): String {
        val ciphertextBytes = Base64.getDecoder().decode(base64Ciphertext)
        val hybridDecrypt = keysetHandle.getPrimitive(HybridDecrypt::class.java)
        
        val decryptedBytes = hybridDecrypt.decrypt(ciphertextBytes, "SecureP2PContext".toByteArray())
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
