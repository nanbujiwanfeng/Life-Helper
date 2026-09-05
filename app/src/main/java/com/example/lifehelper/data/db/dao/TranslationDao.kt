package com.example.lifehelper.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lifehelper.data.db.entity.TranslationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TranslationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TranslationRecord): Long

    @Delete
    suspend fun delete(record: TranslationRecord)

    @Query("DELETE FROM translation_records")
    suspend fun deleteAll()
}
