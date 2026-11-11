package com.example.securemessenger.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object SecureDatabaseKeyManager {

    private const val PREFS_FILE = "secure_db_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase_bytes"

    /**
     * Retrieves the existing SQLCipher passphrase from hardware-backed encrypted storage,
     * or generates a new 32-byte random key if none exists.
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val sharedPrefs = getEncryptedSharedPreferences(context)
        var passphraseString = sharedPrefs.getString(KEY_PASSPHRASE, null)

        if (passphraseString.isNullOrEmpty()) {
            // Generate a secure 32-byte (256-bit) random key using SecureRandom
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)

            // Safely encode raw byte array into an ISO-8859-1 string to persist in SharedPreferences
            passphraseString = String(keyBytes, Charsets.ISO_8859_1)
            
            sharedPrefs.edit()
                .putString(KEY_PASSPHRASE, passphraseString)
                .apply()
        }

        return passphraseString.toByteArray(Charsets.ISO_8859_1)
    }

    /**
     * Creates an instance of EncryptedSharedPreferences backed by Android Keystore's MasterKey (AES256-GCM).
     */
    private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
