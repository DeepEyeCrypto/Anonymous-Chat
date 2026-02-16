package com.phantomnet.app.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkStatus {
    private val _torStatus = MutableStateFlow("Initializing...")
    val torStatus = _torStatus.asStateFlow()

    private val _dhtStatus = MutableStateFlow("Waiting...")
    val dhtStatus = _dhtStatus.asStateFlow()

    fun updateTorStatus(status: String) {
        _torStatus.value = status
    }

    fun updateDhtStatus(status: String) {
        _dhtStatus.value = status
    }
}
