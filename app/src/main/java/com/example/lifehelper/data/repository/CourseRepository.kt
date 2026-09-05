package com.example.lifehelper.data.repository

import com.example.lifehelper.data.db.dao.CourseDao
import com.example.lifehelper.data.db.entity.Course
import kotlinx.coroutines.flow.Flow

class CourseRepository(private val dao: CourseDao) {

    fun getAllCourses(): Flow<List<Course>> = dao.getAllCourses()

    fun getCoursesByDay(day: Int): Flow<List<Course>> = dao.getCoursesByDay(day)

    suspend fun addCourse(course: Course): Long = dao.insert(course)

    suspend fun updateCourse(course: Course) = dao.update(course)

    suspend fun deleteCourse(course: Course) = dao.delete(course)

    suspend fun deleteAll() = dao.deleteAll()
}
