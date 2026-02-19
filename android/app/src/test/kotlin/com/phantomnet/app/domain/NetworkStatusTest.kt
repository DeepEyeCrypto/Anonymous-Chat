package com.phantomnet.app.domain

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NetworkStatusTest {

    @Before
    fun resetState() {
        NetworkStatus.updateTorStatus("Initializing...")
        NetworkStatus.updateDhtStatus("Waiting...")
        NetworkStatus.updateMeshStatus("Inactive")
    }

    @Test
    fun `default values are deterministic`() {
        assertEquals("Initializing...", NetworkStatus.torStatus.value)
        assertEquals("Waiting...", NetworkStatus.dhtStatus.value)
        assertEquals("Inactive", NetworkStatus.meshStatus.value)
    }

    @Test
    fun `update methods publish latest status`() {
        NetworkStatus.updateTorStatus("Connected")
        NetworkStatus.updateDhtStatus("Bootstrapped")
        NetworkStatus.updateMeshStatus("Scanning")

        assertEquals("Connected", NetworkStatus.torStatus.value)
        assertEquals("Bootstrapped", NetworkStatus.dhtStatus.value)
        assertEquals("Scanning", NetworkStatus.meshStatus.value)
    }
}
