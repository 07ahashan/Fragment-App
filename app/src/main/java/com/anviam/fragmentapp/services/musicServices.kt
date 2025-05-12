package com.anviam.fragmentapp.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.anviam.fragmentapp.R

class musicServices: Service() {

    var binder: IBinder? = null
    private var mediaPlayer :MediaPlayer ?= null

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_music)
        mediaPlayer?.isLooping = true
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1,createNotification())
        mediaPlayer?.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        super.onDestroy()
    }

    fun createNotification(): Notification {
        val channelId = "music_channel"
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Player")
            .setContentText("Playing Music...")
            .setSmallIcon(R.drawable.baseline_campaign_24)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Prevents crashes
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel", // Channel ID (Must match the one used in createNotification())
                "Music Playback", // Channel Name
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

}