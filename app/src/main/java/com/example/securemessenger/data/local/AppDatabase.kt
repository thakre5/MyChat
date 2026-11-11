package com.example.securemessenger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportFactory

@Database(
    entities = [MessageEntity::class, ContactEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "secure_p2p_vault.db"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // 1. Fetch or generate the hardware-protected AES-256 key
            val passphrase = SecureDatabaseKeyManager.getOrCreatePassphrase(context)

            // 2. Initialize SQLCipher open helper factory with the raw key bytes
            val factory = SupportFactory(passphrase)

            // 3. Build the Room database instance tied to the SQLCipher factory
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
