# PHANTOM NET v2.0: ORCHESTRATION

> **Status**: ACTIVE  
> **Version**: 2.0.0  
> **Date**: 2026-02-20  
> **Focus**: Frontend/data split, SQLCipher schema, ViewModel contracts, repository interfaces, security model for Phase 1.

---

## 1. COMPONENT MAP

```
┌─────────────────────────────────────────────────┐
│  UI LAYER (Compose) → ViewModels → UseCases     │
├─────────────────────────────────────────────────┤
│  DOMAIN LAYER: Repositories (interfaces), Models │
├─────────────────────────────────────────────────┤
│  DATA LAYER: Room/SQLCipher, EncryptedPrefs, JNI │
├─────────────────────────────────────────────────┤
│  RUST NATIVE CORE (phantom-core, libsignal-ffi)  │
└─────────────────────────────────────────────────┘
```

All local. No server. No cloud. In Phase 1, no network messages.

---

## 2. NAVIGATION ROUTES

```kotlin
sealed interface PhantomRoute {
    @Serializable data object Splash : PhantomRoute
    @Serializable data object OnboardingWelcome : PhantomRoute
    @Serializable data object OnboardingPromise : PhantomRoute
    @Serializable data object OnboardingIdentity : PhantomRoute
    @Serializable data object ChatsList : PhantomRoute
    @Serializable data class ChatDetail(val conversationId: String) : PhantomRoute
    @Serializable data class DcNetRoom(val roomName: String) : PhantomRoute
    @Serializable data object Discovery : PhantomRoute
    @Serializable data object Vault : PhantomRoute
    @Serializable data object Settings : PhantomRoute
    @Serializable data object PrivacyDashboard : PhantomRoute
    @Serializable data object ShardWizard : PhantomRoute
    @Serializable data object IdentityDetail : PhantomRoute
}
```

---

## 3. VIEWMODEL CONTRACTS

| ViewModel | UiState | Events | Side Effects |
|-----------|---------|--------|-------------|
| SplashViewModel | Loading / Ready(hasPersona) | onInitComplete | Read EncryptedPrefs |
| OnboardingViewModel | Idle / Generating / Created(fingerprint) / Error(msg) | generateIdentity | JNI key gen → store |
| ChatsViewModel | conversations, tor/dht/meshStatus | refresh, onConvClick | Observe ConversationDao |
| ChatViewModel | messages, inputText | sendMessage | Encrypt → insert DB |
| SettingsViewModel | fingerprint, coreAvailable, appVersion | copyFingerprint, wipeIdentity | Clipboard, sentinel wipe |

---

## 4. DATABASE SCHEMA (Room + SQLCipher)

### personas

| Column | Type | Notes |
|--------|------|-------|
| id (PK) | TEXT | UUID |
| publicKeyX25519 | BLOB | 32 bytes |
| publicKeyKyber | BLOB | 1184 bytes |
| privateKeyEncrypted | BLOB | Encrypted with device master |
| fingerprint | TEXT | SHA-256 truncated 16 hex chars |
| createdAt | INTEGER | Epoch millis |
| isActive | INTEGER | Boolean (0/1) |

### conversations

| Column | Type | Notes |
|--------|------|-------|
| id (PK) | TEXT | UUID |
| contactName | TEXT | User-chosen display name |
| contactFingerprint | TEXT | Their pubkey fingerprint |
| contactPublicKey | BLOB | Their X25519 public key |
| lastMessagePreview | TEXT | Truncated plaintext |
| lastMessageTimestamp | INTEGER | Epoch millis |
| unreadCount | INTEGER | Badge count |
| isOnline | INTEGER | Boolean |
| routingMode | TEXT | onion/dht/mesh/dcnet |

### messages

| Column | Type | Notes |
|--------|------|-------|
| id (PK) | TEXT | UUID |
| conversationId (FK) | TEXT | → conversations.id, CASCADE |
| senderId | TEXT | "me" or contact fingerprint |
| contentPlaintext | TEXT | Decrypted (DB-level encryption via SQLCipher) |
| contentCiphertext | BLOB | Nullable, original for relay |
| timestamp | INTEGER | Epoch millis |
| isMe | INTEGER | Boolean |
| status | TEXT | sending/sent/delivered/read/failed |
| expiresAt | INTEGER | Nullable, epoch millis (Phase 3) |

**Index**: messages(conversationId) for fast conversation loading.

---

## 5. REPOSITORY INTERFACES

```kotlin
interface PersonaRepository {
    fun getActivePersona(): Flow<Persona?>
    suspend fun hasPersona(): Boolean
    suspend fun createPersona(): Persona
    suspend fun wipeAll()
}

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(id: String): Flow<Conversation?>
    suspend fun createConversation(contactName: String, contactPublicKey: ByteArray): Conversation
    suspend fun markRead(id: String)
    suspend fun wipeAll()
}

interface MessageRepository {
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, plaintext: String, recipientKey: String): Message
    suspend fun wipeAll()
}
```

---

## 6. DOMAIN MODELS

```kotlin
data class Persona(
    val id: String,
    val fingerprint: String,
    val publicKeyX25519: ByteArray,
    val publicKeyKyber: ByteArray,
    val createdAt: Long
)

data class Conversation(
    val id: String,
    val contactName: String,
    val contactFingerprint: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val routingMode: RoutingMode = RoutingMode.DHT
)

enum class RoutingMode { ONION, DHT, MESH, DCNET }

data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val expiresAt: Long? = null
)

enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }
```

---

## 7. MODULE STRUCTURE

```
android/
├── app/
├── core/
│   ├── crypto/        (existing — SignalBridge)
│   ├── network/       (existing — Tor, DHT, Mesh services)
│   ├── database/      (NEW — Room + SQLCipher)
│   └── identity/      (NEW — Persona lifecycle)
├── feature/
│   ├── onboarding/    (NEW — Splash + onboarding screens)
│   └── settings/      (NEW — Settings + wipe)
├── build.gradle.kts
└── settings.gradle.kts
```

Dependency: `app → feature:* → core:identity → core:database + core:crypto`

---

## 8. ENCRYPTED PREFS KEYS

| Key | Type | Purpose |
|-----|------|---------|
| persona_root_key | ByteArray | Master derivation key |
| persona_fingerprint | String | Quick read without DB |
| onboarding_completed | Boolean | Skip onboarding |
| db_passphrase | String | SHA-256(root_key) for SQLCipher |

---

## 9. SECURITY

### Logging policy

- Log: lifecycle events, connection status, error types
- NEVER log: keys, plaintext, ciphertext, PII, peer IDs

### Wipe sequence

1. Rust sentinel shreds key material
2. Room DAOs delete all rows
3. EncryptedPrefs cleared
4. DB file deleted from filesystem
5. Navigate to onboarding (fresh start)

---

## 10. PERSISTENCE LIFECYCLE

| Event | Action |
|-------|--------|
| First launch | Onboarding → keygen → EncryptedPrefs → create DB → insert Persona |
| Subsequent launch | Read prefs → derive passphrase → open DB → load Persona |
| Send message | Encrypt → insert (SENDING) → update (SENT) |
| Wipe | Full §9 sequence |
| App killed | No action — Room persists, StateFlow reloads on resume |

---
*Next: WORLD — CI/CD updates, dependency management, release strategy.*
