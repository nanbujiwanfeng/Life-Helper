package com.example.lifehelper.data

import android.content.Context
import com.example.lifehelper.AppContainer
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.data.db.entity.Goal
import com.example.lifehelper.data.db.entity.Transaction
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * 首次启动示例数据填充器
 * 仅在首次运行且数据库为空时插入示例数据，避免空白首屏
 */
object SampleDataSeeder {

    private const val PREFS_NAME = "lifehelper_prefs"
    private const val KEY_SEEDED = "sample_seeded"

    suspend fun seedIfNeeded(context: Context, container: AppContainer) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        // 学习目标示例（含 1 条逾期）
        if (container.goalRepository.getAllGoals().first().isEmpty()) {
            container.goalRepository.addGoal(
                Goal(
                    title = "复习考研数学第一章",
                    description = "完成高数极限与连续章节习题",
                    deadlineDate = now + 3 * day,
                    progress = 40,
                    priority = 3
                )
            )
            container.goalRepository.addGoal(
                Goal(
                    title = "背英语单词 500 个",
                    description = "使用 APP 每日打卡",
                    deadlineDate = now + 7 * day,
                    progress = 60,
                    priority = 2
                )
            )
            container.goalRepository.addGoal(
                Goal(
                    title = "整理数据结构错题本",
                    description = "示例：已逾期目标",
                    deadlineDate = now - 2 * day,
                    progress = 30,
                    priority = 2
                )
            )
            container.goalRepository.addGoal(
                Goal(
                    title = "完成操作系统绪论笔记",
                    description = "示例：已完成目标",
                    deadlineDate = now - 5 * day,
                    progress = 100,
                    isCompleted = true,
                    priority = 1
                )
            )
        }

        // 课程表示例
        if (container.courseRepository.getAllCourses().first().isEmpty()) {
            container.courseRepository.addCourse(
                Course(name = "高等数学", location = "教学楼A-301", teacher = "王老师",
                    dayOfWeek = 1, startTime = "08:00", endTime = "09:40", weekPattern = "all", remindMinutes = 15)
            )
            container.courseRepository.addCourse(
                Course(name = "数据结构", location = "教学楼B-205", teacher = "李老师",
                    dayOfWeek = 3, startTime = "10:00", endTime = "11:40", weekPattern = "all", remindMinutes = 15)
            )
            container.courseRepository.addCourse(
                Course(name = "英语", location = "语音楼-101", teacher = "陈老师",
                    dayOfWeek = 5, startTime = "14:00", endTime = "15:40", weekPattern = "odd", remindMinutes = 10)
            )
        }

        // 记账示例
        if (container.transactionRepository.getAllTransactions().first().isEmpty()) {
            container.transactionRepository.addTransaction(
                Transaction(type = Transaction.TYPE_EXPENSE, amount = 25.0, category = "餐饮", date = now, note = "午餐")
            )
            container.transactionRepository.addTransaction(
                Transaction(type = Transaction.TYPE_EXPENSE, amount = 8.0, category = "交通", date = now - day, note = "地铁")
            )
            container.transactionRepository.addTransaction(
                Transaction(type = Transaction.TYPE_INCOME, amount = 3000.0, category = "工资", date = now - 3 * day, note = "兼职")
            )
        }

        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }
}
