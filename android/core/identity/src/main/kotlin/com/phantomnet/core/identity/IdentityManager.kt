package com.phantomnet.core.identity

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.phantomnet.core.database.PhantomDatabase
import com.phantomnet.core.database.PhantomDatabaseFactory
import com.phantomnet.core.database.entity.PersonaEntity
import com.phantomnet.core.database.model.Persona
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import kotlinx.coroutines.flow.first
import com.phantomnet.core.database.entity.ConversationEntity
import com.phantomnet.core.database.entity.RoomEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages the full persona lifecycle:
 * - Key generation and storage
 * - Database passphrase derivation
 * - Identity fingerprint computation
 * - Wipe / destroy
 */
class IdentityManager private constructor(
    private val context: Context,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val TAG = "IdentityManager"
        private const val PREF_FILE = "phantom_identity"
        private const val KEY_ROOT = "persona_root_key"
        private const val KEY_FINGERPRINT = "persona_fingerprint"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
        private const val KEY_STEALTH_MODE = "stealth_mode" // 0: Normal, 1: Disguised (Calc)
        private const val KEY_PUBLIC_KEY = "persona_public_key"
        private const val KEY_MIXNET_ENABLED = "mixnet_enabled"
        private const val KEY_PARANOIA_MODE = "paranoia_mode"
        private const val KEY_PIN_REAL_HASH = "pin_real_hash"
        private const val KEY_PIN_DECOY_HASH = "pin_decoy_hash"
        private const val KEY_ROOT_DECOY = "persona_root_key_decoy"
        private const val KEY_FINGERPRINT_DECOY = "persona_fingerprint_decoy"
        private const val KEY_ACTIVE_TYPE = "active_persona_type" // 0: real, 1: decoy

        @Volatile
        private var INSTANCE: IdentityManager? = null

        fun getInstance(context: Context): IdentityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: create(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun create(context: Context): IdentityManager {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            return IdentityManager(context, prefs)
        }
    }

    /** Whether onboarding has been completed (persona exists). */
    val isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    /** Quick access to the stored fingerprint without opening the DB. */
    val fingerprint: String?
        get() = prefs.getString(KEY_FINGERPRINT, null)

    /** Quick access to the public key. */
    val publicKeyX25519: ByteArray?
        get() = prefs.getString(KEY_PUBLIC_KEY, null)?.let { android.util.Base64.decode(it, android.util.Base64.NO_WRAP) }

    /** Current stealth mode: 0 (Normal), 1 (Disguised/Calc) */
    var stealthMode: Int
        get() = prefs.getInt(KEY_STEALTH_MODE, 0)
        set(value) {
            prefs.edit().putInt(KEY_STEALTH_MODE, value).apply()
            // Update Launcher Icon
            val aliasMode = when(value) {
                1 -> StealthManager.AliasMode.CALCULATOR
                2 -> StealthManager.AliasMode.SYSTEM
                else -> StealthManager.AliasMode.NORMAL
            }
            StealthManager.setAlias(context, aliasMode)
        }

    /** Which persona is currently "mounted": 0 (Real), 1 (Decoy) */
    var activePersonaType: Int
        get() = prefs.getInt(KEY_ACTIVE_TYPE, 0)
        private set(value) {
            prefs.edit().putInt(KEY_ACTIVE_TYPE, value).apply()
        }

    var mixnetEnabled: Boolean
        get() = prefs.getBoolean(KEY_MIXNET_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_MIXNET_ENABLED, value).apply()
        }

    var paranoiaMode: Boolean
        get() = prefs.getBoolean(KEY_PARANOIA_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_PARANOIA_MODE, value).apply()
        }

    /**
     * Get the database, opening it with the derived passphrase.
     * Returns null if no persona exists yet.
     */
    val rootKey: String? get() = prefs.getString(KEY_ROOT, null)

    fun getDatabase(): PhantomDatabase? {
        val rootKey = if (activePersonaType == 1) {
            prefs.getString(KEY_ROOT_DECOY, null)
        } else {
            prefs.getString(KEY_ROOT, null)
        } ?: return null
        
        val passphrase = derivePassphrase(rootKey)
        // Use a different DB name for the decoy to isolate files
        val dbSuffix = if (activePersonaType == 1) "_decoy" else ""
        return PhantomDatabaseFactory.getInstance(context, passphrase, dbSuffix)
    }

    /**
     * Unlock the application. The PIN provided determines which persona is mounted.
     * @return true if unlock successful, false if PIN invalid
     */
    fun unlock(pin: String): Boolean {
        val pinHash = sha256Hex(pin.toByteArray())
        val realHash = prefs.getString(KEY_PIN_REAL_HASH, null)
        val decoyHash = prefs.getString(KEY_PIN_DECOY_HASH, null)

        return when {
            pinHash == realHash -> {
                activePersonaType = 0
                true
            }
            pinHash == decoyHash -> {
                activePersonaType = 1
                true
            }
            else -> false
        }
    }

    /**
     * Set the PIN for the real identity (required for stealth).
     */
    fun setRealPin(pin: String) {
        prefs.edit().putString(KEY_PIN_REAL_HASH, sha256Hex(pin.toByteArray())).apply()
    }

    /**
     * Set the PIN for the decoy identity.
     */
    fun setDecoyPin(pin: String) {
        prefs.edit().putString(KEY_PIN_DECOY_HASH, sha256Hex(pin.toByteArray())).apply()
    }

    /**
     * Observe the active persona as a Flow.
     */
    fun observePersona(): Flow<Persona?>? {
        val db = getDatabase() ?: return null
        return db.personaDao().getActivePersona().map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Generate a new persona identity.
     * Creates cryptographic keys, stores them, and initializes the database.
     *
     * @return The newly created Persona
     */
    suspend fun createPersona(): Persona = withContext(Dispatchers.IO) {
        // 1. Generate a random root key (32 bytes) for DB encryption
        val rootKeyBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val rootKeyHex = rootKeyBytes.joinToString("") { "%02x".format(it) }

        // 2. Generate Hybrid X3DH Prekey Bundles via SignalBridge
        val containerJson = com.phantomnet.core.crypto.SignalBridge.generatePrekeyBundleSafe()
        val containerObj = org.json.JSONObject(containerJson)
        val publicBundle = containerObj.getJSONObject("public_bundle")
        val secretBundle = containerObj.getJSONObject("secret_bundle")
        
        val ikBase64 = publicBundle.getString("identity_key")
        val publicX25519 = android.util.Base64.decode(ikBase64, android.util.Base64.DEFAULT)
        val publicKyber = android.util.Base64.decode(publicBundle.getString("identity_key_kyber"), android.util.Base64.DEFAULT)

        // 3. Compute fingerprint (SHA-256 of IK, truncated to 16 hex chars)
        val fingerprintFull = sha256Hex(publicX25519)
        val fingerprint = fingerprintFull.take(16)

        // 4. Store root key in encrypted prefs
        prefs.edit()
            .putString(KEY_ROOT, rootKeyHex)
            .putString(KEY_FINGERPRINT, fingerprint)
            .putString(KEY_PUBLIC_KEY, android.util.Base64.encodeToString(publicX25519, android.util.Base64.NO_WRAP))
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .apply()

        // 5. Create database and insert persona
        val passphrase = derivePassphrase(rootKeyHex)
        val db = PhantomDatabaseFactory.getInstance(context, passphrase)

        val personaId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = PersonaEntity(
            id = personaId,
            publicKeyX25519 = publicX25519,
            publicKeyKyber = publicKyber,
            privateKeyEncrypted = ByteArray(64).also { SecureRandom().nextBytes(it) }, // Master-wrapped mock
            fingerprint = fingerprint,
            prekeyBundleJson = publicBundle.toString(),
            secretBundleJson = secretBundle.toString(),
            createdAt = now,
            isActive = true
        )

        db.personaDao().insert(entity)
        Log.i(TAG, "Persona created: $fingerprint with Prekey Bundle")

        entity.toDomain()
    }

    /**
     * Generate a secondary decoy persona.
     * This identity is used when the decoy PIN is entered.
     */
    suspend fun createDecoyPersona(): Persona = withContext(Dispatchers.IO) {
        // 1. Generate a random root key for Decoy DB
        val rootKeyBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val rootKeyHex = rootKeyBytes.joinToString("") { "%02x".format(it) }

        // 2. Generate separate decoy keys
        val containerJson = com.phantomnet.core.crypto.SignalBridge.generatePrekeyBundleSafe()
        val containerObj = org.json.JSONObject(containerJson)
        val publicBundle = containerObj.getJSONObject("public_bundle")
        val secretBundle = containerObj.getJSONObject("secret_bundle")
        
        val ikBase64 = publicBundle.getString("identity_key")
        val publicX25519 = android.util.Base64.decode(ikBase64, android.util.Base64.DEFAULT)
        val publicKyber = android.util.Base64.decode(publicBundle.getString("identity_key_kyber"), android.util.Base64.DEFAULT)

        val fingerprint = sha256Hex(publicX25519).take(16)

        // 3. Store in decoy slots
        prefs.edit()
            .putString(KEY_ROOT_DECOY, rootKeyHex)
            .putString(KEY_FINGERPRINT_DECOY, fingerprint)
            .apply()

        // 4. Create decoy database and insert persona
        activePersonaType = 1
        val passphrase = derivePassphrase(rootKeyHex)
        val db = PhantomDatabaseFactory.getInstance(context, passphrase, "_decoy")

        val entity = PersonaEntity(
            id = UUID.randomUUID().toString(),
            publicKeyX25519 = publicX25519,
            publicKeyKyber = publicKyber,
            privateKeyEncrypted = ByteArray(64).also { SecureRandom().nextBytes(it) },
            fingerprint = fingerprint,
            prekeyBundleJson = publicBundle.toString(),
            secretBundleJson = secretBundle.toString(),
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        db.personaDao().insert(entity)
        Log.i(TAG, "Decoy Persona created: $fingerprint")

        entity.toDomain()
    }



    /**
     * Export the full persona credentials for Multi-Device Sync.
     * Stage 2: Includes active conversations and rooms.
     */
    suspend fun exportPersonaBundle(): String? = withContext(Dispatchers.IO) {
        val rootKey = prefs.getString(KEY_ROOT, null) ?: return@withContext null
        val db = getDatabase() ?: return@withContext null
        val entity = db.personaDao().getActivePersona().first() ?: return@withContext null

        val bundle = JSONObject().apply {
            put("root_key", rootKey)
            put("fingerprint", entity.fingerprint)
            put("public_x25519", android.util.Base64.encodeToString(entity.publicKeyX25519, android.util.Base64.DEFAULT))
            put("public_kyber", android.util.Base64.encodeToString(entity.publicKeyKyber, android.util.Base64.DEFAULT))
            put("secret_bundle", entity.secretBundleJson)
            put("prekey_bundle", entity.prekeyBundleJson)

            // Stage 2: Sync Conversations
            val convs = db.conversationDao().getAll().first()
            val convArray = JSONArray()
            convs.forEach { c ->
                convArray.put(JSONObject().apply {
                    put("id", c.id)
                    put("name", c.contactName)
                    put("fingerprint", c.contactFingerprint)
                    put("public_key", android.util.Base64.encodeToString(c.contactPublicKey, android.util.Base64.DEFAULT))
                    put("preview", c.lastMessagePreview)
                    put("timestamp", c.lastMessageTimestamp)
                    put("mode", c.routingMode)
                    c.sharedSecret?.let { 
                        put("secret", android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT))
                    }
                })
            }
            put("conversations", convArray)

            // Stage 2: Sync Rooms
            val rooms = db.roomDao().getAll().first()
            val roomArray = JSONArray()
            rooms.forEach { r ->
                roomArray.put(JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("type", r.type)
                    put("secrets", r.sharedSecretsJson)
                    put("preview", r.lastMessagePreview)
                    put("timestamp", r.lastMessageTimestamp)
                })
            }
            put("rooms", roomArray)
        }
        bundle.toString()
    }

    /**
     * Import a persona bundle from another device.
     * Stage 2: Unpacks and inserts conversations and rooms.
     */
    suspend fun importPersonaBundle(bundleJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject(bundleJson)
            val rootKey = obj.getString("root_key")
            val fingerprint = obj.getString("fingerprint")
            
            // 1. Wipe existing local data
            wipeAll()

            // 2. Store new root identity in prefs
            prefs.edit()
                .putString(KEY_ROOT, rootKey)
                .putString(KEY_FINGERPRINT, fingerprint)
                .putString(KEY_PUBLIC_KEY, obj.getString("public_x25519"))
                .putBoolean(KEY_ONBOARDING_DONE, true)
                .apply()

            // 3. Initialize DB and insert the identity
            val passphrase = derivePassphrase(rootKey)
            val db = PhantomDatabaseFactory.getInstance(context, passphrase)
            
            val identity = PersonaEntity(
                id = UUID.randomUUID().toString(),
                publicKeyX25519 = android.util.Base64.decode(obj.getString("public_x25519"), android.util.Base64.DEFAULT),
                publicKeyKyber = android.util.Base64.decode(obj.getString("public_kyber"), android.util.Base64.DEFAULT),
                privateKeyEncrypted = ByteArray(64).also { SecureRandom().nextBytes(it) }, 
                fingerprint = fingerprint,
                prekeyBundleJson = obj.getString("prekey_bundle"),
                secretBundleJson = obj.getString("secret_bundle"),
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            db.personaDao().insert(identity)

            // Stage 2: Restore Conversations
            val convs = obj.optJSONArray("conversations")
            if (convs != null) {
                for (i in 0 until convs.length()) {
                    val c = convs.getJSONObject(i)
                    db.conversationDao().upsert(ConversationEntity(
                        id = c.getString("id"),
                        contactName = c.getString("name"),
                        contactFingerprint = c.getString("fingerprint"),
                        contactPublicKey = android.util.Base64.decode(c.getString("public_key"), android.util.Base64.DEFAULT),
                        lastMessagePreview = c.getString("preview"),
                        lastMessageTimestamp = c.getLong("timestamp"),
                        unreadCount = 0,
                        isOnline = false,
                        routingMode = c.getString("mode"),
                        sharedSecret = c.optString("secret", null)?.let { 
                            android.util.Base64.decode(it, android.util.Base64.DEFAULT) 
                        }
                    ))
                }
            }

            // Stage 2: Restore Rooms
            val rooms = obj.optJSONArray("rooms")
            if (rooms != null) {
                for (i in 0 until rooms.length()) {
                    val r = rooms.getJSONObject(i)
                    db.roomDao().upsert(RoomEntity(
                        id = r.getString("id"),
                        name = r.getString("name"),
                        type = r.getString("type"),
                        sharedSecretsJson = r.getString("secrets"),
                        lastMessagePreview = r.getString("preview"),
                        lastMessageTimestamp = r.getLong("timestamp"),
                        unreadCount = 0,
                        isActive = true
                    ))
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import persona", e)
            false
        }
    }

    /**
     * Stage 3: Export message history for all conversations.
     * This prepares a payload of historical messages for a linked device.
     */
    suspend fun exportHistoryBundle(): String? = withContext(Dispatchers.IO) {
        val db = getDatabase() ?: return@withContext null
        try {
            val convs = db.conversationDao().getAll().first()
            val historyObj = JSONObject()
            val historyArray = JSONArray()

            for (conv in convs) {
                val messages = db.messageDao().getRecentForConversation(conv.id, 50).first()
                val convHistory = JSONObject().apply {
                    put("conversation_id", conv.id)
                    val msgArray = JSONArray()
                    messages.forEach { m ->
                        msgArray.put(JSONObject().apply {
                            put("id", m.id)
                            put("sender", m.senderId)
                            put("content", m.contentPlaintext)
                            put("timestamp", m.timestamp)
                            put("is_me", m.isMe)
                            put("status", m.status)
                        })
                    }
                    put("messages", msgArray)
                }
                historyArray.put(convHistory)
            }
            historyObj.put("history", historyArray)
            historyObj.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export history", e)
            null
        }
    }

    /**
     * Stage 3: Ingest message history bundle.
     */
    suspend fun importHistoryBundle(historyJson: String): Boolean = withContext(Dispatchers.IO) {
        val db = getDatabase() ?: return@withContext false
        try {
            val obj = JSONObject(historyJson)
            val historyArray = obj.getJSONArray("history")
            for (i in 0 until historyArray.length()) {
                val convHistory = historyArray.getJSONObject(i)
                val convId = convHistory.getString("conversation_id")
                val messages = convHistory.getJSONArray("messages")

                for (j in 0 until messages.length()) {
                    val m = messages.getJSONObject(j)
                    db.messageDao().insert(com.phantomnet.core.database.entity.MessageEntity(
                        id = m.getString("id"),
                        conversationId = convId,
                        senderId = m.getString("sender"),
                        contentPlaintext = m.getString("content"),
                        contentCiphertext = null,
                        timestamp = m.getLong("timestamp"),
                        isMe = m.getBoolean("is_me"),
                        status = m.getString("status"),
                        expiresAt = null
                    ))
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import history", e)
            false
        }
    }

    /**
     * Wipe all identity data — keys, database, preferences.
     * This is irreversible.
     */
    suspend fun wipeAll() = withContext(Dispatchers.IO) {
        try {
            // 1. Clear database
            val db = getDatabase()
            if (db != null) {
                db.messageDao().deleteAll()
                db.conversationDao().deleteAll()
                db.personaDao().deleteAll()
            }

            // 2. Destroy database file
            PhantomDatabaseFactory.destroy(context)

            // 3. Clear encrypted preferences
            prefs.edit().clear().apply()

            Log.i(TAG, "Identity wiped")
        } catch (t: Throwable) {
            Log.e(TAG, "Error during wipe", t)
            // Force delete even if DB operations fail
            PhantomDatabaseFactory.destroy(context)
            prefs.edit().clear().apply()
        }
    }

    // --- Private helpers ---

    private fun derivePassphrase(rootKeyHex: String): ByteArray {
        return sha256Bytes("phantom_db_$rootKeyHex")
    }

    private fun sha256Hex(input: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun sha256Bytes(input: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    }

    private fun PersonaEntity.toDomain() = Persona(
        id = id,
        fingerprint = fingerprint,
        publicKeyX25519 = publicKeyX25519,
        publicKeyKyber = publicKeyKyber,
        createdAt = createdAt
    )
}
