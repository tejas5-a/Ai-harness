package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AltronTaskExecutionService : Service {

  constructor() : super()

  private var wakeLock: PowerManager.WakeLock? = null

  companion object {
    const val CHANNEL_ID = "altron_tasks_channel"
    const val NOTIFICATION_ID = 2001

    const val ACTION_START_TASK = "com.example.action.START_TASK"
    const val ACTION_UPDATE_TASK = "com.example.action.UPDATE_TASK"
    const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_PROGRESS = "extra_progress"
    const val EXTRA_OPERATION = "extra_operation"

    fun startOrUpdate(
      context: Context,
      taskId: String,
      title: String,
      progress: Int,
      operation: String
    ) {
      val intent = Intent(context, AltronTaskExecutionService::class.java).apply {
        action = ACTION_UPDATE_TASK
        putExtra(EXTRA_TASK_ID, taskId)
        putExtra(EXTRA_TASK_TITLE, title)
        putExtra(EXTRA_PROGRESS, progress)
        putExtra(EXTRA_OPERATION, operation)
      }
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
      } catch (e: Exception) {
        // Handle background start restrictions gracefully
      }
    }

    fun stop(context: Context) {
      val intent = Intent(context, AltronTaskExecutionService::class.java).apply {
        action = ACTION_STOP_SERVICE
      }
      try {
        context.startService(intent)
      } catch (ignored: Exception) {}
    }
  }

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    acquireWakeLock()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_SERVICE) {
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    val title = intent?.getStringExtra(EXTRA_TASK_TITLE) ?: "Background Coding Task"
    val progress = intent?.getIntExtra(EXTRA_PROGRESS, 0) ?: 0
    val operation = intent?.getStringExtra(EXTRA_OPERATION) ?: "Processing changes..."

    val notification = buildForegroundNotification(title, progress, operation)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    return START_STICKY
  }

  override fun onDestroy() {
    releaseWakeLock()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun acquireWakeLock() {
    try {
      val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
      wakeLock = powerManager?.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "AltronCode:TaskWakeLock"
      )?.apply {
        acquire(30 * 60 * 1000L) // 30 min max safety timeout
      }
    } catch (ignored: Exception) {}
  }

  private fun releaseWakeLock() {
    try {
      if (wakeLock?.isHeld == true) {
        wakeLock?.release()
      }
    } catch (ignored: Exception) {}
    wakeLock = null
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Altron Background Tasks",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Shows live progress of autonomous background AI coding tasks"
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildForegroundNotification(
    title: String,
    progress: Int,
    operation: String
  ): android.app.Notification {
    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingOpen = PendingIntent.getActivity(
      this,
      0,
      openIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Altron Code: $title")
      .setContentText("$operation ($progress%)")
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setProgress(100, progress, false)
      .setContentIntent(pendingOpen)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .build()
  }
}
