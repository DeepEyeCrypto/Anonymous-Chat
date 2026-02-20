package com.phantomnet.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.database.model.Message
import com.phantomnet.core.database.repository.ConversationRepository
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.network.MailboxManager
import com.phantomnet.core.crypto.SignalBridge
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)
    private var repository: ConversationRepository? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentConversationId: String? = null

    init {
        val db = identityManager.getDatabase()
        if (db != null) {
            repository = ConversationRepository(db)
        }
    }

    fun loadMessages(conversationId: String) {
        currentConversationId = conversationId
        val repo = repository ?: return
        
        viewModelScope.launch {
            repo.getMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(text: String, recipientFingerprint: String) {
        val convId = currentConversationId ?: return
        val repo = repository ?: return

        viewModelScope.launch {
            // 1. Save to Local DB (Optimistic)
            repo.sendMessage(convId, text)

            val conv = repo.getConversationSync(convId) ?: return@launch
            
            try {
                var sharedSecretBase64 = conv.sharedSecret?.let { 
                    android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) 
                }

                if (sharedSecretBase64 == null) {
                    // HYBRID HANDSHAKE REQUIRED (Phase 5)
                    Log.i("ChatViewModel", "No shared secret. Initiating Hybrid PQC Handshake with $recipientFingerprint...")
                    
                    // a) Fetch Identity Bundle from DHT
                    MailboxManager.fetchPeerIdentity(recipientFingerprint)
                    
                    // b) Poll for result (simplified wait loop)
                    var bundle: com.phantomnet.core.network.IdentityBundle? = null
                    for (i in 0 until 10) { 
                        kotlinx.coroutines.delay(1000)
                        bundle = MailboxManager.pollIdentity(recipientFingerprint)
                        if (bundle != null) break
                    }

                    if (bundle == null || bundle.prekeyBundleJson == null) {
                        Log.e("ChatViewModel", "Handshake failed: Peer bundle not found in DHT")
                        return@launch
                    }

                    // c) Hybrid Key Agreement
                    val db = identityManager.getDatabase() ?: return@launch
                    val persona = db.personaDao().getActivePersona().firstOrNull() ?: return@launch
                    
                    val secretBundleObj = org.json.JSONObject(persona.secretBundleJson ?: return@launch)
                    val myIkSecret = secretBundleObj.getString("identity_key_secret")
                    
                    val bundleObj = org.json.JSONObject(bundle.prekeyBundleJson!!)
                    val theirSpkX25519 = bundleObj.getString("signed_prekey")
                    val theirSpkKyber = bundleObj.getString("signed_prekey_kyber")
                    
                    // 1. Classical X25519 Diffie-Hellman (IK_A * SPK_B)
                    // Note: In real X3DH we do more steps, but this is our Hybrid Phase 5 base.
                    val ssX25519 = SignalBridge.deriveSharedSecretSafe(myIkSecret, theirSpkX25519)
                    
                    // 2. Post-Quantum Kyber Encapsulation
                    val kyberResult = SignalBridge.encapsulateKyberSafe(theirSpkKyber)
                    val kyberObj = org.json.JSONObject(kyberResult)
                    val ssKyber = kyberObj.getString("ss")
                    val ctKyber = kyberObj.getString("ct")
                    
                    // 3. Hybrid Secret Derivation
                    sharedSecretBase64 = SignalBridge.deriveHybridSecretSafe(ssX25519, ssKyber)
                    repo.updateSharedSecret(convId, android.util.Base64.decode(sharedSecretBase64, android.util.Base64.DEFAULT))

                    // d) Construct HANDSHAKE_INIT_HYBRID
                    val myIkPublic = android.util.Base64.encodeToString(persona.publicKeyX25519, android.util.Base64.DEFAULT)
                    
                    // Generate REAL Ephemeral Key Alice (EK_A)
                    val ekResult = SignalBridge.generateEphemeralKeyPairSafe()
                    val ekObj = org.json.JSONObject(ekResult)
                    val ekPublic = ekObj.getString("public")
                    // EK_A secret is not needed further for HANDSHAKE_INIT, Bob will use it with his SPK_B secret.
                    
                    val encryptedMessage = SignalBridge.encryptWithKeySafe(text, sharedSecretBase64!!)
                    
                    val handshakeJson = org.json.JSONObject().apply {
                        put("type", "HANDSHAKE_INIT_HYBRID")
                        put("fp", persona.fingerprint)
                        put("ik", myIkPublic)
                        put("ek", ekPublic) // Sending EK_A Public
                        put("kct", ctKyber) // Kyber Ciphertext for Bob to decapsulate
                        put("msg", encryptedMessage)
                    }

                    MailboxManager.postMessage(
                        recipientFingerprint, 
                        handshakeJson.toString(),
                        identityManager.mixnetEnabled
                    )
                    Log.i("ChatViewModel", "Hybrid Handshake posted to DHT (Mixnet: ${identityManager.mixnetEnabled})")
                } else {
                    // SECURE SESSION EXISTS (Quantum-Hardened)
                    val encrypted = SignalBridge.encryptWithKeySafe(text, sharedSecretBase64!!)
                    val messageJson = org.json.JSONObject().apply {
                        put("type", "MSG")
                        put("fp", identityManager.fingerprint)
                        put("msg", encrypted)
                    }
                    MailboxManager.postMessage(
                        recipientFingerprint, 
                        messageJson.toString(),
                        identityManager.mixnetEnabled
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Encryption/Handshake failed", e)
            }
        }
    }
}
