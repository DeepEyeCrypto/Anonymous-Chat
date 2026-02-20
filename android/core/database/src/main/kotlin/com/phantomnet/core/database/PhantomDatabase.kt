package com.phantomnet.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phantomnet.core.database.dao.RoomDao
import com.phantomnet.core.database.entity.RoomEntity

@Database(
    entities = [
        PersonaEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        RoomEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhantomDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun roomDao(): RoomDao
}

/**
 * Factory to create the encrypted Room database.
 *
 * The passphrase should be derived from the persona root key stored
 * in EncryptedSharedPreferences. This keeps the DB unreadable without
 * the Android Keystore master key.
 */
object PhantomDatabaseFactory {
    
    private val instances = mutableMapOf<String, PhantomDatabase>()

    fun getInstance(context: Context, passphrase: ByteArray, nameSuffix: String = ""): PhantomDatabase {
        val dbName = "phantom$nameSuffix.db"
        return synchronized(this) {
            instances.getOrPut(dbName) {
                buildDatabase(context, passphrase, dbName)
            }
        }
    }

    private fun buildDatabase(context: Context, passphrase: ByteArray, dbName: String): PhantomDatabase {
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context.applicationContext,
            PhantomDatabase::class.java,
            dbName
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    fun destroy(context: Context) {
        synchronized(this) {
            instances.forEach { (name, db) ->
                db.close()
                context.deleteDatabase(name)
            }
            instances.clear()
        }
    }
}
