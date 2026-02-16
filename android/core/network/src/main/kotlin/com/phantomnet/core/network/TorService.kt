package com.phantomnet.core.network

class TorService {
    companion object {
        @JvmStatic
        external fun startTor(cacheDir: String): String
    }
}
