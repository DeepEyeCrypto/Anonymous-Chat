package com.phantomnet.core.network

object DhtService {
    init {
        try {
            System.loadLibrary("kademlia_dht")
        } catch (e: UnsatisfiedLinkError) {
            // Log or handle
        }
    }

    @JvmStatic
    external fun startDhtNode(): String

    @JvmStatic
    external fun addBootstrapNode(peerId: String, address: String)

    @JvmStatic
    external fun announceToBootstrap(url: String)

    /**
     * Store a value in the DHT under a specific key.
     * Uses Quorum::One for speed; content is verified via E2EE layer.
     */
    @JvmStatic
    external fun putValue(key: String, value: String)

    /**
     * Initiate an asynchronous Get operation for a key.
     * Results must be retrieved via [pollValue].
     */
    @JvmStatic
    external fun getValue(key: String)

    /**
     * Poll for the result of a [getValue] operation.
     * Returns the value string if found, otherwise an empty string.
     */
    @JvmStatic
    external fun pollValue(key: String): String
}
