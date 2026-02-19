package com.phantomnet.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phantomnet.core.database.dao.ConversationDao
import com.phantomnet.core.database.dao.MessageDao
import com.phantomnet.core.database.dao.PersonaDao
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.MessageEntity
import com.phantomnet.core.database.entity.PersonaEntity
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        PersonaEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhantomDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}

/**
 * Factory to create the encrypted Room database.
 *
 * The passphrase should be derived from the persona root key stored
 * in EncryptedSharedPreferences. This keeps the DB unreadable without
 * the Android Keystore master key.
 */
object PhantomDatabaseFactory {

    @Volatile
    private var INSTANCE: PhantomDatabase? = null

    /**
     * Get or create the encrypted database instance.
     *
     * @param context Application context
     * @param passphrase The SQLCipher passphrase (derived from persona root key)
     */
    fun getInstance(context: Context, passphrase: ByteArray): PhantomDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context, passphrase).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(context: Context, passphrase: ByteArray): PhantomDatabase {
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            PhantomDatabase::class.java,
            "phantom.db"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()  // acceptable in alpha
            .build()
    }

    /**
     * Close and destroy the database instance.
     * Called during identity wipe.
     */
    fun destroy(context: Context) {
        INSTANCE?.close()
        INSTANCE = null
        context.deleteDatabase("phantom.db")
    }
}
