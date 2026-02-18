package com.phantomnet.core

import android.util.Log

/**
 * Kotlin wrapper for the Phantom Net Rust Core (FFI).
 */
object PhantomCore {
    private const val TAG = "PhantomCore"

    init {
        try {
            System.loadLibrary("phantom_core")
            Log.i(TAG, "Successfully loaded phantom_core native library")
            initLogging()
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load phantom_core native library: ${e.message}")
        }
    }

    /**
     * Initializes the Rust logger (android_logger) for Logcat output.
     */
    external fun initLogging()

    /**
     * Generates a new Post-Quantum (Kyber) Identity.
     * @return Serialized identity string.
     */
    external fun generateIdentity(): String

    /**
     * Splits a secret into N shards requiring T for reconstruction.
     */
    external fun splitSecret(secret: String, threshold: Int, total: Int): String

    /**
     * Computes the DC-Net contribution for an untraceable room round.
     */
    external fun computeDcNetContribution(myId: Int, message: String): String

    /**
     * Runs a privacy audit and returns a JSON report with score and recommendations.
     */
    external fun runPrivacyAudit(configJson: String): String

    /**
     * Triggers a sentinel action (e.g., 1 = PANIC WIPE).
     */
    external fun triggerSentinelAction(actionType: Int)
}
