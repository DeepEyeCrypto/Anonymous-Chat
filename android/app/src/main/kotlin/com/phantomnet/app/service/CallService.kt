package com.phantomnet.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.phantomnet.app.ui.call.PrivacyMode
import kotlinx.coroutines.*

class CallService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_START_CALL = "ACTION_START_CALL"
        const val ACTION_ACCEPT_CALL = "ACTION_ACCEPT_CALL"
        const val ACTION_DECLINE_CALL = "ACTION_DECLINE_CALL"
        const val ACTION_END_CALL = "ACTION_END_CALL"

        const val EXTRA_CONTACT_NAME = "EXTRA_CONTACT_NAME"
        const val EXTRA_IS_VIDEO = "EXTRA_IS_VIDEO"
        const val EXTRA_MODE = "EXTRA_MODE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_CALL -> {
                val contact = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: "Unknown"
                val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false)
                val mode = intent.getStringExtra(EXTRA_MODE) ?: PrivacyMode.PRIVATE.name
                
                // Keep the service alive strictly as a foreground service
                startForeground(1001, createNotification(contact, "Ringing..."))
                
                // Initialize NativeMediaEngine and generate offer logic here
            }
            ACTION_ACCEPT_CALL -> {
                updateNotification("Call in progress...")
                // Process Answer logic here
            }
            ACTION_DECLINE_CALL, ACTION_END_CALL -> {
                endCall()
            }
        }
        return START_NOT_STICKY
    }

    private fun endCall() {
        // Run any needed native teardown inside serviceScope (e.g. NativeMediaEngine.destroyEngine())
        serviceScope.launch {
            // Wait for DB logging if necessary
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "call_service_channel",
            "Active Calls",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(title: String, content: String): Notification {
        // PendingIntent logic intended to reopen the app to CallOrchestratorScreen
        return NotificationCompat.Builder(this, "call_service_channel")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification("Active Call", content)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }
}
