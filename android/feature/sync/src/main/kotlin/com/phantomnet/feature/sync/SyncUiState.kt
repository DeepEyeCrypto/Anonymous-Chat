package com.phantomnet.feature.sync

data class SyncUiState(
    val exportBundle: String? = null,
    val historyBundle: String? = null,
    val isProcessing: Boolean = false,
    val isHistorySyncing: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false,
    val isHistoryComplete: Boolean = false,
    val mode: SyncMode = SyncMode.Idle
)

enum class SyncMode {
    Idle, Exporting, Importing
}
