package com.phantomnet.app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkStatus {
    private val _torStatus = MutableStateFlow("Initializing...")
    val torStatus = _torStatus.asStateFlow()

    private val _dhtStatus = MutableStateFlow("Waiting...")
    val dhtStatus = _dhtStatus.asStateFlow()
    
    private val _meshStatus = MutableStateFlow("Inactive")
    val meshStatus = _meshStatus.asStateFlow()

    fun updateTorStatus(status: String) {
        _torStatus.value = status
    }

    fun updateDhtStatus(status: String) {
        _dhtStatus.value = status
    }
    
    fun updateMeshStatus(status: String) {
        _meshStatus.value = status
    }
}
