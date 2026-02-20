package com.phantomnet.core.database.repository

import com.phantomnet.core.database.dao.CallLogDao
import com.phantomnet.core.database.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

class CallRepository(private val callLogDao: CallLogDao) {

    fun getLogsForConversation(conversationId: String): Flow<List<CallLogEntity>> {
        return callLogDao.getLogsForConversation(conversationId)
    }

    suspend fun logCallStart(
        sessionId: String,
        conversationId: String,
        direction: String,
        privacyMode: String
    ) {
        val entity = CallLogEntity(
            sessionId = sessionId,
            conversationId = conversationId,
            direction = direction,
            durationSec = 0,
            timestamp = System.currentTimeMillis(),
            privacyMode = privacyMode,
            outcome = "ONGOING"
        )
        callLogDao.insertCallLog(entity)
    }

    suspend fun logCallEnd(
        sessionId: String,
        durationSec: Int,
        outcome: String
    ) {
        callLogDao.updateCallEnd(sessionId, durationSec, outcome)
    }

    suspend fun clearAllCallLogs() {
        callLogDao.deleteAll()
    }
}
