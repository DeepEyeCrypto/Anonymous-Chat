package com.phantomnet.core.network

class DhtService {
    companion object {
        @JvmStatic
        external fun startDhtNode(): String

        @JvmStatic
        external fun addBootstrapNode(peerId: String, address: String)

        @JvmStatic
        external fun announceToBootstrap(url: String)
    }
}
