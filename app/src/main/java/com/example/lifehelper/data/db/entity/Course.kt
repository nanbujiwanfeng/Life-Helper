package com.example.lifehelper.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 课程实体
 * @property dayOfWeek 星期几：1=周一 ... 7=周日
 * @property startTime 开始时间 "HH:mm"（24 小时制）
 * @property endTime 结束时间 "HH:mm"
 * @property weekPattern 周次模式："all" 每周、"odd" 单周、"even" 双周、或 "1-16" 指定周数范围
 * @property remindMinutes 课前提醒提前分钟数，0 表示不提醒
 */
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val weekPattern: String = "all",
    val remindMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 将 "HH:mm" 转为当天从 0 点起的分钟数，便于比较排序 */
    fun startMinutes(): Int = timeToMinutes(startTime)

    fun endMinutes(): Int = timeToMinutes(endTime)

    companion object {
        fun timeToMinutes(hhmm: String): Int {
            val parts = hhmm.split(":")
            if (parts.size < 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }

        fun minutesToTime(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return "%02d:%02d".format(h, m)
        }
    }
}
