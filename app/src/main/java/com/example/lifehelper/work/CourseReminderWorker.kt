package com.example.lifehelper.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifehelper.R
import com.example.lifehelper.notification.NotificationHelper

/**
 * 课程上课提醒 Worker：由 ReminderScheduler 在课前定时触发
 */
class CourseReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val name = inputData.getString("courseName") ?: return Result.success()
        val location = inputData.getString("location") ?: ""
        val startTime = inputData.getString("startTime") ?: ""

        val title = applicationContext.getString(R.string.notif_course_title)
        val body = applicationContext.getString(R.string.notif_course_body, name, startTime)
        val fullBody = if (location.isNotBlank()) "$body（$location）" else body

        NotificationHelper.showNotification(
            applicationContext,
            NotificationHelper.CHANNEL_COURSE,
            title,
            fullBody,
            notificationId = "course_${inputData.getString("courseId") ?: name}".hashCode()
        )
        return Result.success()
    }
}
