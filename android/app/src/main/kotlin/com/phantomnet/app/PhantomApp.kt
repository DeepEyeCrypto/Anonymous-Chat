package com.phantomnet.app

import android.app.Application
import android.util.Log
import com.phantomnet.app.domain.NetworkStatus
import com.phantomnet.core.network.DhtService
import com.phantomnet.core.network.TorService
import com.phantomnet.core.network.MeshService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PhantomApp : Application() {
    companion object {
        private const val TAG = "PhantomNet"
        
        init {
            try {
                System.loadLibrary("libsignal_ffi")
                System.loadLibrary("kademlia_dht")
                System.loadLibrary("tor_client")
                System.loadLibrary("mesh_protocol")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native libraries", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startCoreServices()
    }

    private fun startCoreServices() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Start Tor (Needs cache dir)
            try {
                NetworkStatus.updateTorStatus("Bootstrapping...")
                val cacheDir = File(cacheDir, "tor_data")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val status = TorService.startTor(cacheDir.absolutePath)
                NetworkStatus.updateTorStatus(status)
                Log.i(TAG, "Tor Status: $status")
            } catch (e: Exception) {
                NetworkStatus.updateTorStatus("Failed: ${e.message}")
                Log.e(TAG, "Failed to start Tor", e)
            }

            // 2. Start DHT (Independent for now, later will route through Tor)
            try {
                NetworkStatus.updateDhtStatus("Starting Node...")
                val status = DhtService.startDhtNode()
                NetworkStatus.updateDhtStatus(status)
                Log.i(TAG, "DHT Status: $status")
                
                // Auto-bootstrap (for demo)
                DhtService.announceToBootstrap("http://10.0.2.2:3000") // 10.0.2.2 is localhost from Emulator
            } catch (e: Exception) {
                NetworkStatus.updateDhtStatus("Failed: ${e.message}")
                Log.e(TAG, "Failed to start DHT Node", e)
            }
            
            // 3. Start Mesh (Stub/Simulation for now)
            try {
                NetworkStatus.updateMeshStatus("Scanning...")
                val status = MeshService.startMesh()
                NetworkStatus.updateMeshStatus(status)
                Log.i(TAG, "Mesh Status: $status")
            } catch (e: Exception) {
                NetworkStatus.updateMeshStatus("Failed: ${e.message}")
                Log.e(TAG, "Failed to start Mesh Service", e)
            }
        }
    }
}
