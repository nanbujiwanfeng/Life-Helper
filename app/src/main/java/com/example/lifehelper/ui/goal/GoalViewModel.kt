package com.example.lifehelper.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifehelper.LifeHelperApp
import com.example.lifehelper.data.db.entity.Goal
import com.example.lifehelper.data.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 目标筛选状态 */
enum class GoalFilter { ALL, ONGOING, DONE, EXPIRED }

class GoalViewModel(private val repository: GoalRepository) : ViewModel() {

    val goals: StateFlow<List<Goal>> = repository.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow(GoalFilter.ALL)
    val filter: StateFlow<GoalFilter> = _filter.asStateFlow()

    fun setFilter(f: GoalFilter) {
        _filter.value = f
    }

    fun addGoal(goal: Goal) = viewModelScope.launch { repository.addGoal(goal) }

    fun updateGoal(goal: Goal) = viewModelScope.launch { repository.updateGoal(goal) }

    fun deleteGoal(goal: Goal) = viewModelScope.launch { repository.deleteGoal(goal) }

    fun toggleComplete(goal: Goal) = viewModelScope.launch {
        repository.updateGoal(
            goal.copy(
                isCompleted = !goal.isCompleted,
                progress = if (!goal.isCompleted) 100 else goal.progress
            )
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LifeHelperApp
                GoalViewModel(app.container.goalRepository)
            }
        }
    }
}
