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

    /**
     * Get the database, opening it with the derived passphrase.
     * Returns null if no persona exists yet.
     */
    fun getDatabase(): PhantomDatabase? {
        val rootKey = prefs.getString(KEY_ROOT, null) ?: return null
        val passphrase = derivePassphrase(rootKey)
        return PhantomDatabaseFactory.getInstance(context, passphrase)
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
        // 1. Generate a random root key (32 bytes)
        val rootKeyBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val rootKeyHex = rootKeyBytes.joinToString("") { "%02x".format(it) }

        // 2. Generate PQC-style identity keys
        //    In production, this would call PhantomCore.generateIdentitySafe()
        //    For Phase 1, we generate X25519-sized mock keys
        val publicX25519 = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val publicKyber = ByteArray(32).also { SecureRandom().nextBytes(it) }  // simplified for Phase 1
        val privateKeyEncrypted = ByteArray(64).also { SecureRandom().nextBytes(it) }

        // 3. Compute fingerprint (SHA-256 of public key, truncated to 16 hex chars)
        val fingerprintFull = sha256Hex(publicX25519)
        val fingerprint = fingerprintFull.take(16)

        // 4. Store root key in encrypted prefs
        prefs.edit()
            .putString(KEY_ROOT, rootKeyHex)
            .putString(KEY_FINGERPRINT, fingerprint)
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .apply()

        // 5. Create database with derived passphrase and insert persona
        val passphrase = derivePassphrase(rootKeyHex)
        val db = PhantomDatabaseFactory.getInstance(context, passphrase)

        val personaId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = PersonaEntity(
            id = personaId,
            publicKeyX25519 = publicX25519,
            publicKeyKyber = publicKyber,
            privateKeyEncrypted = privateKeyEncrypted,
            fingerprint = fingerprint,
            createdAt = now,
            isActive = true
        )

        db.personaDao().insert(entity)

        Log.i(TAG, "Persona created: $fingerprint")

        entity.toDomain()
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
