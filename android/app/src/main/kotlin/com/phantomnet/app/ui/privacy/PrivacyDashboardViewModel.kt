package com.phantomnet.app.ui.privacy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phantomnet.core.PhantomCore
import com.phantomnet.core.identity.IdentityManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuditReport(
    val risk_score: Int,
    val status_color: String,
    val note: String? = null,
    val vulnerabilities: List<String> = emptyList()
)

sealed class AuditState {
    object Idle : AuditState()
    object Scanning : AuditState()
    data class Success(val report: AuditReport) : AuditState()
    data class Error(val message: String) : AuditState()
}

class PrivacyDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val identityManager = IdentityManager.getInstance(application)

    private val _auditState = MutableStateFlow<AuditState>(AuditState.Idle)
    val auditState: StateFlow<AuditState> = _auditState.asStateFlow()

    fun runAudit() {
        viewModelScope.launch {
            _auditState.value = AuditState.Scanning
            delay(2000) // Visual feedback for scanning

            val config = """{
                "use_mixnet": ${identityManager.mixnetEnabled},
                "use_psi": true,
                "is_sharded": ${identityManager.rootKey != null},
                "cover_traffic_enabled": ${identityManager.paranoiaMode},
                "selected_adversary": "GlobalSurveillance"
            }"""

            try {
                val resultJson = PhantomCore.runPrivacyAuditSafe(config)
                val report = Json { ignoreUnknownKeys = true }.decodeFromString<AuditReport>(resultJson)
                _auditState.value = AuditState.Success(report)
            } catch (e: Exception) {
                _auditState.value = AuditState.Error(e.message ?: "Audit engine failed")
            }
        }
    }

    fun triggerPanicAction(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Native Shredding (Rust Core)
            PhantomCore.triggerSentinelActionSafe(1) // 1 = SHRED_IDENTITY
            
            // 2. Clear App Data (Kotlin Layer)
            identityManager.wipeAll()
            
            // 3. Callback to UI (Reset navigation)
            onComplete()
        }
    }
}
