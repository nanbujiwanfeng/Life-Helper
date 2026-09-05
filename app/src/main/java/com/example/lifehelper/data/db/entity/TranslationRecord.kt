package com.example.lifehelper.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 翻译历史记录实体
 */
@Entity(tableName = "translation_records")
data class TranslationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val timestamp: Long = System.currentTimeMillis()
)
