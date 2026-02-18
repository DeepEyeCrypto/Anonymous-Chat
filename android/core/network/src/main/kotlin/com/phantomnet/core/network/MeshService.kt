package com.phantomnet.core.network

object MeshService {
    @Volatile
    private var onDeviceFoundListener: ((String) -> Unit)? = null

    init {
        System.loadLibrary("mesh_protocol")
    }

    @JvmStatic
    external fun startMesh(): String

    fun setOnDeviceFoundListener(listener: ((String) -> Unit)?) {
        onDeviceFoundListener = listener
    }

    @JvmStatic
    fun onDeviceFound(name: String) {
        onDeviceFoundListener?.invoke(name)
    }
}
