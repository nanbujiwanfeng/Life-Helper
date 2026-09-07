package com.example.lifehelper.ui.course

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifehelper.LifeHelperApp
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.data.repository.CourseRepository
import com.example.lifehelper.data.repository.ProfileRepository
import com.example.lifehelper.work.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CourseViewModel(
    private val repository: CourseRepository,
    private val profileRepository: ProfileRepository,
    private val appContext: Context
) : ViewModel() {

    val courses: StateFlow<List<Course>> = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 当前教学周（第几周，1 起），默认第 1 周 */
    private val _currentWeek = MutableStateFlow(profileRepository.getCurrentWeek())
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    fun setCurrentWeek(week: Int) {
        val w = week.coerceIn(1, 30)
        _currentWeek.value = w
        profileRepository.setCurrentWeek(w)
    }

    fun previousWeek() = setCurrentWeek(_currentWeek.value - 1)
    fun nextWeek() = setCurrentWeek(_currentWeek.value + 1)

    /** 添加课程，返回是否成功（false 表示存在时间冲突） */
    fun addCourse(course: Course, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        if (hasConflict(courses.value, course)) {
            onResult(false)
        } else {
            repository.addCourse(course)
            ReminderScheduler.scheduleCourseReminder(appContext, course)
            onResult(true)
        }
    }

    /** 更新课程，返回是否成功 */
    fun updateCourse(course: Course, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        if (hasConflict(courses.value, course)) {
            onResult(false)
        } else {
            repository.updateCourse(course)
            ReminderScheduler.scheduleCourseReminder(appContext, course)
            onResult(true)
        }
    }

    fun deleteCourse(course: Course) = viewModelScope.launch {
        repository.deleteCourse(course)
        ReminderScheduler.cancelCourseReminder(appContext, course.id)
    }

    /** 课程冲突检测：同一天且时间区间重叠 */
    private fun hasConflict(courses: List<Course>, course: Course): Boolean {
        val start = course.startMinutes()
        val end = course.endMinutes()
        return courses.any { existing ->
            existing.id != course.id &&
                existing.dayOfWeek == course.dayOfWeek &&
                existing.startMinutes() < end &&
                start < existing.endMinutes()
        }
    }

    companion object {
        /** 返回今天对应的星期（1=周一 ... 7=周日） */
        fun todayDayOfWeek(): Int {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // 1=周日 ... 7=周六
            return ((day + 5) % 7) + 1
        }

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LifeHelperApp
                CourseViewModel(app.container.courseRepository, app.container.profileRepository, app)
            }
        }
    }
}
