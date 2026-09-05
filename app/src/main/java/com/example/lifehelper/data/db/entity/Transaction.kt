package com.example.lifehelper.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收支账目实体
 * @property type 类型："INCOME" 收入 / "EXPENSE" 支出
 * @property amount 金额（正数）
 * @property date 交易日期（epoch 毫秒）
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_INCOME = "INCOME"
        const val TYPE_EXPENSE = "EXPENSE"
    }
}
