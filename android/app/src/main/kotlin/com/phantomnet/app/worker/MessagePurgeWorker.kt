package com.phantomnet.app.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import com.phantomnet.core.identity.IdentityManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy

/**
 * Periodically purges expired messages from the local database.
 * Implements the Phase 3 "Ephemeral" security requirement.
 */
class MessagePurgeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MessagePurgeWorker>(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "phantom_message_purge",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.i("MessagePurgeWorker", "Starting scheduled message purge...")
        
        val identityManager = IdentityManager.getInstance(applicationContext)
        val db = identityManager.getDatabase() ?: return Result.success()
        
        try {
            val now = System.currentTimeMillis()
            // We'll need to add a purgeExpired method to MessageDao or use raw query
            db.openHelper.writableDatabase.execSQL(
                "DELETE FROM messages WHERE expiresAt IS NOT NULL AND expiresAt < $now"
            )
            Log.i("MessagePurgeWorker", "Purge complete. Expired messages shredded.")
        } catch (e: Exception) {
            Log.e("MessagePurgeWorker", "Purge failed", e)
            return Result.retry()
        }
        
        return Result.success()
    }
}
