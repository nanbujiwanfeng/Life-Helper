package com.example.lifehelper.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lifehelper.R
import com.example.lifehelper.data.db.AppDatabase
import com.example.lifehelper.notification.NotificationHelper
import kotlinx.coroutines.flow.first

/**
 * 目标提醒 Worker：每日检查一次，提醒即将到期或已逾期的目标
 */
class GoalReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val goals = try {
            db.goalDao().getAllGoals().first()
        } catch (e: Exception) {
            return Result.retry()
        }

        val now = System.currentTimeMillis()
        val twoDaysLater = now + 2L * 24 * 60 * 60 * 1000

        // 即将到期（2 天内）或已逾期且未完成的目标
        val urgent = goals.filter { goal ->
            !goal.isCompleted && goal.deadlineDate != null &&
                goal.deadlineDate!! in now..twoDaysLater
        }
        val overdue = goals.filter { goal ->
            !goal.isCompleted && goal.deadlineDate != null && goal.deadlineDate!! < now
        }

        if (urgent.isNotEmpty() || overdue.isNotEmpty()) {
            val title = applicationContext.getString(R.string.notif_goal_title)
            val text = buildString {
                if (overdue.isNotEmpty()) {
                    append("${overdue.size} 个目标已逾期")
                }
                if (urgent.isNotEmpty()) {
                    if (isNotEmpty()) append("，")
                    append("${urgent.size} 个目标即将到期")
                }
                append("，点击查看详情")
            }
            NotificationHelper.showNotification(
                applicationContext,
                NotificationHelper.CHANNEL_GOAL,
                title,
                text,
                notificationId = 1001
            )
        }
        return Result.success()
    }
}
