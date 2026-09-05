package com.example.lifehelper.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.lifehelper.data.db.entity.Course
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 提醒调度器：负责为课程安排课前提醒、为目标安排每日检查
 */
object ReminderScheduler {

    private const val COURSE_WORK_PREFIX = "course_reminder_"
    private const val GOAL_WORK_NAME = "goal_daily_check"

    /**
     * 为课程安排课前提醒。
     * 若 remindMinutes <= 0 则取消提醒。
     */
    fun scheduleCourseReminder(context: Context, course: Course) {
        if (course.remindMinutes <= 0) {
            cancelCourseReminder(context, course.id)
            return
        }

        val start = computeNextCourseStart(course.dayOfWeek, course.startTime)
        val remindAt = start.timeInMillis - course.remindMinutes * 60_000L
        val delay = remindAt - System.currentTimeMillis()
        if (delay <= 0) return // 已过本次提醒时间，不安排（用户下次编辑时重新安排）

        val data = workDataOf(
            "courseId" to course.id,
            "courseName" to course.name,
            "location" to course.location,
            "startTime" to course.startTime
        )
        val request = OneTimeWorkRequestBuilder<CourseReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("course_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            COURSE_WORK_PREFIX + course.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelCourseReminder(context: Context, courseId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(COURSE_WORK_PREFIX + courseId)
    }

    /** 安排每日目标检查（幂等，重复调用不会叠加） */
    fun scheduleDailyGoalCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<GoalReminderWorker>(1, TimeUnit.DAYS)
            .addTag("goal_check")
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            GOAL_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * 计算下一次上课的具体时间点。
     * dayOfWeek：1=周一 ... 7=周日
     */
    private fun computeNextCourseStart(dayOfWeek: Int, hhmm: String): Calendar {
        val now = Calendar.getInstance()
        val target = now.clone() as Calendar

        val calDay = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }

        var daysUntil = (calDay - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
        if (daysUntil == 0) {
            // 今天是该星期，判断是否已过开始时间
            val startMinutes = Course.timeToMinutes(hhmm)
            val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            if (startMinutes <= nowMinutes) daysUntil = 7
        }

        target.add(Calendar.DAY_OF_YEAR, daysUntil)
        val totalMinutes = Course.timeToMinutes(hhmm)
        target.set(Calendar.HOUR_OF_DAY, totalMinutes / 60)
        target.set(Calendar.MINUTE, totalMinutes % 60)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)
        return target
    }
}
