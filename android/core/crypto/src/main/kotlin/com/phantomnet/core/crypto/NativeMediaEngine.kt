package com.phantomnet.core.crypto

/**
 * Configuration options for the NativeMediaEngine.
 */
data class MediaEngineConfig(
    val iceTransportPolicy: String, // "all" | "relay" | "none"
    val useSyntheticDtlsTunnel: Boolean, // True for PARANOID mode
    val stunServers: List<String> = emptyList(),
    val turnServers: List<String> = emptyList()
)

/**
 * JNI Stub for the low-level WebRTC / DTLS-SRTP media engine.
 * Phantom Net uses this explicitly stripped and secured media stack.
 */
object NativeMediaEngine {

    /**
     * Initializes the native WebRTC engine with the specified policy.
     * Must be called before any media tracking or rendering begins.
     */
    @JvmStatic
    external fun initializeEngine(
        iceTransportPolicy: String,
        useSyntheticDtlsTunnel: Boolean
    )

    /**
     * Cleans up the native media engine. Destroys all peer connection context.
     */
    @JvmStatic
    external fun destroyEngine()

    /**
     * Generates a Session Description Protocol (SDP) Offer.
     */
    @JvmStatic
    external fun createOffer(requestVideo: Boolean): String

    /**
     * Processes a remote Answer SDP.
     */
    @JvmStatic
    external fun processAnswer(sdp: String)

    /**
     * Receives a remote SDP Offer and returns a negotiated SDP Answer.
     */
    @JvmStatic
    external fun receiveOffer(sdp: String): String

    /**
     * Ingest an ICE candidate from the remote peer.
     */
    @JvmStatic
    external fun addIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String)

    /**
     * Audio device control: Mute or Unmute the microphone track.
     */
    @JvmStatic
    external fun toggleMicrophone(muted: Boolean)

    /**
     * Video device control: Enable or Disable local camera track.
     */
    @JvmStatic
    external fun toggleCamera(enabled: Boolean)

    /**
     * Switches between front and back camera (if available).
     */
    @JvmStatic
    external fun switchCamera()

    // JNI library loading usually happens once globally in PhantomCore,
    // but just to be safe if this is ever isolated:
    init {
        try {
            System.loadLibrary("phantom_core")
        } catch (e: UnsatisfiedLinkError) {
            // Ignore if already loaded by PhantomCore
        }
    }
}
