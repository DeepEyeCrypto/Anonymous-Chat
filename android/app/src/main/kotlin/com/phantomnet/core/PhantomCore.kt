package com.phantomnet.core

import android.util.Log

/**
 * Kotlin wrapper for the Phantom Net Rust Core (FFI).
 *
 * All native calls are guarded so that if the .so is missing or any JNI
 * function is not linked, the caller gets a clear fallback instead of a crash.
 */
object PhantomCore {
    private const val TAG = "PhantomCore"

    /** true only when phantom_core.so was loaded AND initLogging() succeeded */
    @Volatile
    var isAvailable: Boolean = false
        private set

    init {
        try {
            System.loadLibrary("phantom_core")
            initLogging()            // only call if loadLibrary succeeded
            isAvailable = true
            Log.i(TAG, "phantom_core native library ready")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "phantom_core unavailable: ${e.message}")
        } catch (t: Throwable) {
            Log.e(TAG, "phantom_core init error: ${t.message}")
        }
    }

    /* -------- guarded public API -------- */

    fun generateIdentitySafe(): String {
        if (!isAvailable) return "unavailable: native lib missing"
        return try { generateIdentity() } catch (t: Throwable) { "error: ${t.message}" }
    }

    fun splitSecretSafe(secret: String, threshold: Int, total: Int): String {
        if (!isAvailable) return "Secure backup unavailable on this build."
        return try { splitSecret(secret, threshold, total) } catch (t: Throwable) { "error: ${t.message}" }
    }

    fun computeDcNetContributionSafe(myId: Int, message: String): String {
        if (!isAvailable) return "DC-Net unavailable"
        return try { computeDcNetContribution(myId, message) } catch (t: Throwable) { "error: ${t.message}" }
    }

    fun aggregateDcNetContributionsSafe(contributions: Array<String>): String {
        if (!isAvailable) return "DC-Net aggregation unavailable"
        return try { aggregateDcNetContributions(contributions) } catch (t: Throwable) { "error: ${t.message}" }
    }

    fun runPrivacyAuditSafe(configJson: String): String {
        if (!isAvailable) return """{"risk_score":50,"status_color":"#FFD600","note":"native lib missing"}"""
        return try { runPrivacyAudit(configJson) } catch (t: Throwable) { """{"risk_score":50,"status_color":"#FFD600","error":"${t.message}"}""" }
    }

    fun triggerSentinelActionSafe(actionType: Int) {
        if (!isAvailable) { Log.w(TAG, "triggerSentinelAction skipped: native lib missing"); return }
        try { triggerSentinelAction(actionType) } catch (t: Throwable) { Log.e(TAG, "Sentinel action failed", t) }
    }

    fun runPsiDiscoverySafe(localIdentifiers: Array<String>, remoteBlinded: Array<String>): String {
        if (!isAvailable) return "Discovery unavailable"
        return try { runPsiDiscovery(localIdentifiers, remoteBlinded) } catch (t: Throwable) { "error: ${t.message}" }
    }

    fun initMixnetSafe(intervalMs: Long = 1000, batchSize: Int = 5, paranoia: Boolean = true) {
        if (!isAvailable) return
        try { initMixnet(intervalMs, batchSize, paranoia) } catch (t: Throwable) { Log.e(TAG, "Mixnet init failed", t) }
    }

    fun sendMixnetPacketSafe(payload: String) {
        if (!isAvailable) return
        try { sendMixnetPacket(payload) } catch (t: Throwable) { Log.e(TAG, "Mixnet send failed", t) }
    }

    /* -------- raw external fns (private — use *Safe wrappers above) -------- */

    private external fun initLogging()
    private external fun generateIdentity(): String
    private external fun splitSecret(secret: String, threshold: Int, total: Int): String
    private external fun computeDcNetContribution(myId: Int, message: String): String
    private external fun aggregateDcNetContributions(contributions: Array<String>): String
    private external fun runPrivacyAudit(configJson: String): String
    private external fun triggerSentinelAction(actionType: Int)
    private external fun runPsiDiscovery(local: Array<String>, remote: Array<String>): String
    private external fun initMixnet(intervalMs: Long, batchSize: Int, paranoia: Boolean)
    private external fun sendMixnetPacket(payload: String)
}
