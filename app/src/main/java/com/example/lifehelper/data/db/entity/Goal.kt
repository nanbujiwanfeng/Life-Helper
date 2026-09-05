package com.example.lifehelper.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学习目标实体
 * @property deadlineDate 截止日期（epoch 毫秒），null 表示无截止日期
 * @property progress 当前进度百分比 0-100
 * @property priority 优先级：1=低 2=中 3=高
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val deadlineDate: Long? = null,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** 是否已过期（未完成且已过截止日期） */
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        if (isCompleted) return false
        val deadline = deadlineDate ?: return false
        return deadline < now
    }
}
