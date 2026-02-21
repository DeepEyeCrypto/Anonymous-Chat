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
        private var dhtNativeLoaded = false
        private var torNativeLoaded = false
        private var meshNativeLoaded = false

        private fun loadNativeLibrary(name: String): Boolean {
            return try {
                System.loadLibrary(name)
                Log.i(TAG, "Loaded native library: $name")
                true
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: $name", e)
                false
            }
        }
        
        init {
            loadNativeLibrary("libsignal_ffi")
            dhtNativeLoaded = loadNativeLibrary("kademlia_dht")
            torNativeLoaded = loadNativeLibrary("tor_client")
            meshNativeLoaded = loadNativeLibrary("mesh_protocol")
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            if (meshNativeLoaded) {
                try {
                    MeshService.setOnDeviceFoundListener { name ->
                        NetworkStatus.updateMeshStatus("Found: $name")
                    }
                } catch (t: Throwable) {
                    NetworkStatus.updateMeshStatus("Unavailable: ${t.message}")
                    Log.e(TAG, "Mesh callback setup failed", t)
                }
            } else {
                NetworkStatus.updateMeshStatus("Unavailable: native lib missing")
            }

            startCoreServices()
            
            // Wire MailboxManager mixnet hook (breaks circular dep core:network → app)
            com.phantomnet.core.network.MailboxManager.mixnetSender = { payload ->
                com.phantomnet.core.PhantomCore.sendMixnetPacketSafe(payload)
            }
            
            // Phase 2: Background Polling
            com.phantomnet.app.worker.DhtPollingWorker.schedule(this)
            // Phase 3: Ephemeral Purge
            com.phantomnet.app.worker.MessagePurgeWorker.schedule(this)
        } catch (t: Throwable) {
            Log.e(TAG, "Fatal error during app initialization, continuing with degraded mode", t)
        }
    }

    private fun startCoreServices() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Start Tor (Needs cache dir)
            if (torNativeLoaded) {
                try {
                    NetworkStatus.updateTorStatus("Bootstrapping...")
                    val cacheDir = File(cacheDir, "tor_data")
                    if (!cacheDir.exists()) cacheDir.mkdirs()

                    val status = TorService.startTor(cacheDir.absolutePath)
                    NetworkStatus.updateTorStatus(status)
                    Log.i(TAG, "Tor Status: $status")
                } catch (t: Throwable) {
                    NetworkStatus.updateTorStatus("Failed: ${t.message}")
                    Log.e(TAG, "Failed to start Tor", t)
                }
            } else {
                NetworkStatus.updateTorStatus("Unavailable: native lib missing")
            }

            // 2. Start DHT (Independent for now, later will route through Tor)
            if (dhtNativeLoaded) {
                try {
                    NetworkStatus.updateDhtStatus("Starting Node...")
                    val status = DhtService.startDhtNode()
                    NetworkStatus.updateDhtStatus(status)
                    Log.i(TAG, "DHT Status: $status")

                    // Auto-bootstrap (for demo)
                    DhtService.announceToBootstrap("http://10.0.2.2:3000") // 10.0.2.2 is localhost from Emulator
                } catch (t: Throwable) {
                    NetworkStatus.updateDhtStatus("Failed: ${t.message}")
                    Log.e(TAG, "Failed to start DHT Node", t)
                }
            } else {
                NetworkStatus.updateDhtStatus("Unavailable: native lib missing")
            }
            
            // 3. Start Mesh (Stub/Simulation for now)
            if (meshNativeLoaded) {
                try {
                    NetworkStatus.updateMeshStatus("Scanning...")
                    val status = MeshService.startMesh()
                    NetworkStatus.updateMeshStatus(status)
                    Log.i(TAG, "Mesh Status: $status")
                } catch (t: Throwable) {
                    NetworkStatus.updateMeshStatus("Failed: ${t.message}")
                    Log.e(TAG, "Failed to start Mesh Service", t)
                }
            }
        }
    }
}
