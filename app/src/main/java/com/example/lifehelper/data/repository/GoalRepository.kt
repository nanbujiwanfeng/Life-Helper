package com.example.lifehelper.data.repository

import com.example.lifehelper.data.db.dao.GoalDao
import com.example.lifehelper.data.db.entity.Goal
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val dao: GoalDao) {

    fun getAllGoals(): Flow<List<Goal>> = dao.getAllGoals()

    suspend fun addGoal(goal: Goal): Long = dao.insert(goal)

    suspend fun updateGoal(goal: Goal) = dao.update(goal)

    suspend fun deleteGoal(goal: Goal) = dao.delete(goal)

    suspend fun deleteAll() = dao.deleteAll()
}
