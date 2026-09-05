package com.example.lifehelper.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lifehelper.R

/**
 * 通知工具类：负责创建通知渠道、发送通知
 */
object NotificationHelper {

    const val CHANNEL_COURSE = "course_reminder"
    const val CHANNEL_GOAL = "goal_reminder"

    /** 在应用启动时创建通知渠道（Android 8.0+ 必需） */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val courseChannel = NotificationChannel(
                CHANNEL_COURSE,
                context.getString(R.string.notif_channel_course),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = context.getString(R.string.notif_channel_course) }
            val goalChannel = NotificationChannel(
                CHANNEL_GOAL,
                context.getString(R.string.notif_channel_goal),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_channel_goal) }
            manager.createNotificationChannel(courseChannel)
            manager.createNotificationChannel(goalChannel)
        }
    }

    /** 发送通知；若无通知权限则静默跳过 */
    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        text: String,
        notificationId: Int
    ) {
        // Android 13+ 需要运行时通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // 忽略权限异常
        }
    }
}
