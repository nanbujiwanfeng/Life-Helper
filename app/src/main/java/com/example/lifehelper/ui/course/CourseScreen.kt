package com.example.lifehelper.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifehelper.R
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.ui.theme.ExpenseRed

/** 课程块配色（柔和、按课程 id 稳定分配） */
private val coursePalette = listOf(
    Color(0xFF5B8DEF), Color(0xFF7C6FE8), Color(0xFF26A69A), Color(0xFFF59E0B),
    Color(0xFFEC6B8B), Color(0xFF9575CD), Color(0xFF29B6F6), Color(0xFF66BB6A)
)

private fun courseColor(course: Course): Color =
    coursePalette[(course.id % coursePalette.size).toInt()]

@Composable
private fun dayLabel(day: Int): String = stringResource(
    when (day) {
        1 -> R.string.day_1
        2 -> R.string.day_2
        3 -> R.string.day_3
        4 -> R.string.day_4
        5 -> R.string.day_5
        6 -> R.string.day_6
        else -> R.string.day_7
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(viewModel: CourseViewModel = viewModel(factory = CourseViewModel.Factory)) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var confirmDelete by remember { mutableStateOf<Course?>(null) }
    var showConflict by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.course_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCourse = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.course_add))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (courses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                WeekTimetableGrid(
                    courses = courses,
                    onCourseClick = { course ->
                        editingCourse = course
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog) {
        CourseEditDialog(
            course = editingCourse,
            defaultDay = CourseViewModel.todayDayOfWeek(),
            onDismiss = { showDialog = false },
            onSave = { course ->
                if (editingCourse == null) {
                    viewModel.addCourse(course) { success -> showConflict = !success }
                } else {
                    viewModel.updateCourse(course) { success -> showConflict = !success }
                }
                showDialog = false
            },
            onDelete = {
                editingCourse?.let { course ->
                    showDialog = false
                    confirmDelete = course
                }
            }
        )
    }

    if (showConflict) {
        AlertDialog(
            onDismissRequest = { showConflict = false },
            title = { Text(stringResource(R.string.course_title)) },
            text = { Text(stringResource(R.string.course_conflict)) },
            confirmButton = {
                TextButton(onClick = { showConflict = false }) { Text(stringResource(R.string.action_confirm)) }
            }
        )
    }

    confirmDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCourse(course)
                    confirmDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

/**
 * 周课程表网格：左侧时间轴 + 7 天列，课程块按起止时间定位。
 * 顶部星期表头固定，下方表格可纵向滚动。
 */
@Composable
private fun WeekTimetableGrid(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val hourHeight = 56.dp
    val dayStartMin = 8 * 60   // 08:00
    val dayEndMin = 22 * 60    // 22:00
    val totalHours = (dayEndMin - dayStartMin) / 60
    val timeColWidth = 44.dp
    val headerHeight = 40.dp
    val today = remember { CourseViewModel.todayDayOfWeek() }

    Column(modifier = modifier.fillMaxSize()) {
        // 星期表头（固定，不随纵向滚动）
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(timeColWidth).height(headerHeight))
            for (day in 1..7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(headerHeight)
                        .background(
                            if (day == today) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabel(day),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal,
                        color = if (day == today) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 表格主体（纵向滚动）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // 时间轴
            Column {
                repeat(totalHours) { h ->
                    val hour = dayStartMin / 60 + h
                    Box(
                        modifier = Modifier.width(timeColWidth).height(hourHeight),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Text(
                            text = "$hour:00",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                        )
                    }
                }
            }

            // 7 天列
            for (day in 1..7) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(hourHeight * totalHours)
                ) {
                    courses.filter { it.dayOfWeek == day }.forEach { course ->
                        val startMin = course.startMinutes().coerceAtLeast(dayStartMin)
                        val endMin = course.endMinutes().coerceAtMost(dayEndMin)
                        val top = hourHeight * ((startMin - dayStartMin) / 60f)
                        val rawHeight = hourHeight * ((endMin - startMin).coerceAtLeast(30) / 60f)
                        val blockHeight = rawHeight.coerceAtLeast(26.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp)
                                .offset(y = top)
                                .height(blockHeight)
                                .clip(RoundedCornerShape(6.dp))
                                .background(courseColor(course))
                                .clickable { onCourseClick(course) }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (blockHeight > 44.dp) {
                                Text(
                                    text = "${course.startTime} ${course.location}".trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseEditDialog(
    course: Course?,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var location by remember { mutableStateOf(course?.location ?: "") }
    var teacher by remember { mutableStateOf(course?.teacher ?: "") }
    var day by remember { mutableStateOf(course?.dayOfWeek ?: defaultDay) }
    var startTime by remember { mutableStateOf(course?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(course?.endTime ?: "09:40") }
    var weekPattern by remember { mutableStateOf(course?.weekPattern ?: "all") }
    var remindMinutes by remember { mutableStateOf((course?.remindMinutes ?: 15).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (course == null) R.string.course_add else R.string.course_edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.course_field_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text(stringResource(R.string.course_field_location)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = teacher, onValueChange = { teacher = it },
                    label = { Text(stringResource(R.string.course_field_teacher)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime, onValueChange = { startTime = it },
                        label = { Text(stringResource(R.string.course_field_start)) },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime, onValueChange = { endTime = it },
                        label = { Text(stringResource(R.string.course_field_end)) },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.course_field_day), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (d in 1..7) {
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = {
                                Text(
                                    when (d) {
                                        1 -> stringResource(R.string.day_1)
                                        2 -> stringResource(R.string.day_2)
                                        3 -> stringResource(R.string.day_3)
                                        4 -> stringResource(R.string.day_4)
                                        5 -> stringResource(R.string.day_5)
                                        6 -> stringResource(R.string.day_6)
                                        else -> stringResource(R.string.day_7)
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.course_field_weeks), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = weekPattern == "all", onClick = { weekPattern = "all" },
                        label = { Text(stringResource(R.string.course_week_all)) })
                    FilterChip(selected = weekPattern == "odd", onClick = { weekPattern = "odd" },
                        label = { Text(stringResource(R.string.course_week_odd)) })
                    FilterChip(selected = weekPattern == "even", onClick = { weekPattern = "even" },
                        label = { Text(stringResource(R.string.course_week_even)) })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = remindMinutes, onValueChange = { remindMinutes = it },
                    label = { Text(stringResource(R.string.course_field_remind)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (course != null) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.action_delete), color = ExpenseRed)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        onSave(
                            Course(
                                id = course?.id ?: 0,
                                name = name.trim(),
                                location = location.trim(),
                                teacher = teacher.trim(),
                                dayOfWeek = day,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                weekPattern = weekPattern,
                                remindMinutes = remindMinutes.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
