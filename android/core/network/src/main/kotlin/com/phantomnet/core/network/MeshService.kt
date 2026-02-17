package com.phantomnet.core.network

object MeshService {
    init {
        System.loadLibrary("mesh_protocol")
    }

    @JvmStatic
    external fun startMesh(): String

    @JvmStatic
    fun onDeviceFound(name: String) {
        // Update mesh status or notify listeners
        com.phantomnet.app.domain.NetworkStatus.updateMeshStatus("Found: $name")
    }
}
