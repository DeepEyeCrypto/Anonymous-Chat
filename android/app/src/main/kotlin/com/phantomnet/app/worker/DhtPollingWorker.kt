package com.phantomnet.app.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.network.MailboxManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background worker that ensures:
 * 1. Our DHT identity is refreshed (prevent expiration).
 * 2. New messages are pulled from our DHT mailbox.
 */
class DhtPollingWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "DhtPollingWorker"
        private const val WORK_NAME = "phantom_dht_polling"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<DhtPollingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val identityManager = IdentityManager.getInstance(applicationContext)
        
        // 1. Refresh Identity if it exists
        val persona = identityManager.observePersona()?.first()
        if (persona != null) {
            Log.d(TAG, "Refreshing DHT identity for ${persona.fingerprint}")
            MailboxManager.publishIdentity(persona.fingerprint, persona.publicKeyX25519)
            
            // 2. Poll Mailbox
            Log.d(TAG, "Polling DHT mailbox for ${persona.fingerprint}")
            MailboxManager.fetchMyMessages(persona.fingerprint)
            
            // Give it a second to fetch
            kotlinx.coroutines.delay(2000)
            
            val message = MailboxManager.pollMyMessages(persona.fingerprint)
            if (message != null) {
                Log.i(TAG, "New message found in DHT mailbox! Processing...")
                com.phantomnet.core.network.SecureMessagingProcessor.processIncomingPayload(
                    applicationContext, 
                    message
                )
            }
        }

        Result.success()
    }
}
