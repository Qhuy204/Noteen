package com.example.noteen.viewmodel

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.data.LocalRepository.entity.SubTaskEntity
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.TaskGroupEntity
import com.example.noteen.data.LocalRepository.entity.TaskGroupWithSubTasks
import com.example.noteen.data.LocalRepository.reposity.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

enum class DueDateStatus {
    OVERDUE, UPCOMING, LONG_TERM
}

data class SubTask(
    val id: UUID,
    val taskGroupId: UUID,
    val title: String,
    var isCompleted: Boolean,
    var order: Int
)

data class TaskGroup(
    val id: UUID,
    val title: String,
    val subTasks: List<SubTask>,
    val dueDate: LocalDateTime?,
    var isExpanded: Boolean,
    var isCompleted: Boolean,
    var order: Int
)

// --- Mapper Functions ---
// Chuyển đổi giữa Database Entities và UI Models
@RequiresApi(Build.VERSION_CODES.O)
fun TaskGroupWithSubTasks.toModel(): TaskGroup {
    val sortedSubTasks = subTasks.sortedBy { it.order }.map { it.toModel() }
    return TaskGroup(
        id = this.taskGroup.id,
        title = this.taskGroup.title,
        subTasks = sortedSubTasks,
        dueDate = this.taskGroup.dueDate,
        isExpanded = this.taskGroup.isExpanded,
        isCompleted = this.taskGroup.isCompleted,
        order = this.taskGroup.order
    )
}

fun SubTaskEntity.toModel(): SubTask {
    return SubTask(
        id = this.id,
        taskGroupId = this.taskGroupId,
        title = this.title,
        isCompleted = this.isCompleted,
        order = this.order
    )
}

fun TaskGroup.toEntity(): TaskGroupEntity {
    return TaskGroupEntity(
        id = this.id,
        title = this.title,
        dueDate = this.dueDate,
        isExpanded = this.isExpanded,
        isCompleted = this.isCompleted,
        order = this.order
    )
}

fun SubTask.toEntity(): SubTaskEntity {
    return SubTaskEntity(
        id = this.id,
        taskGroupId = this.taskGroupId,
        title = this.title,
        isCompleted = this.isCompleted,
        order = this.order
    )
}


@RequiresApi(Build.VERSION_CODES.O)
class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val nearestUpcomingTask: StateFlow<TaskGroup?>

    private val _taskGroups = MutableStateFlow<List<TaskGroup>>(emptyList())
    val taskGroups: StateFlow<List<TaskGroup>> = _taskGroups.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _selectedTaskIds = MutableStateFlow<Set<UUID>>(emptySet())
    val selectedTaskIds: StateFlow<Set<UUID>> = _selectedTaskIds.asStateFlow()

    init {
        val taskDao = AppDatabase.getInstance(application).taskDao()
        repository = TaskRepository(taskDao)

        viewModelScope.launch {
            repository.getTaskGroupsWithSubTasks().collect { list ->
                val (completed, todo) = list.map { it.toModel() }.partition { it.isCompleted }
                val sortedTasks = todo.sortedBy { it.order } + completed.sortedByDescending { it.order }
                _taskGroups.value = sortedTasks
            }
        }

        nearestUpcomingTask = repository.getNearestUpcomingTask()
            .map { it?.toModel() }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun enterEditMode() { _isEditMode.value = true }
    fun exitEditMode() {
        _isEditMode.value = false
        _selectedTaskIds.value = emptySet()
    }

    fun toggleSelection(groupId: UUID) {
        _selectedTaskIds.update { currentIds ->
            if (groupId in currentIds) currentIds - groupId else currentIds + groupId
        }
    }

    fun selectAll() {
        val allIds = _taskGroups.value.map { it.id }.toSet()
        if (_selectedTaskIds.value.size == allIds.size) {
            _selectedTaskIds.value = emptySet()
        } else {
            _selectedTaskIds.value = allIds
        }
    }

    fun deleteSelected() = viewModelScope.launch {
        repository.deleteTasks(_selectedTaskIds.value.toList())
        exitEditMode()
    }

    fun toggleMasterCheckbox(groupId: UUID, isChecked: Boolean) = viewModelScope.launch {
        _taskGroups.value.find { it.id == groupId }?.let { group ->
            val updatedSubTasks = group.subTasks.map { it.copy(isCompleted = isChecked) }
            val updatedGroup = group.copy(
                subTasks = updatedSubTasks,
                isCompleted = isChecked,
                isExpanded = if (isChecked) false else group.isExpanded
            )
            repository.updateTask(updatedGroup.toEntity(), updatedSubTasks.map { it.toEntity() })
        }
    }

    fun toggleSubTaskCheckbox(groupId: UUID, subTaskId: UUID) = viewModelScope.launch {
        _taskGroups.value.find { it.id == groupId }?.let { group ->
            val updatedSubTasks = group.subTasks.map { subTask ->
                if (subTask.id == subTaskId) subTask.copy(isCompleted = !subTask.isCompleted) else subTask
            }
            val allCompleted = updatedSubTasks.isNotEmpty() && updatedSubTasks.all { it.isCompleted }
            val updatedGroup = group.copy(subTasks = updatedSubTasks, isCompleted = allCompleted)
            repository.updateTask(updatedGroup.toEntity(), updatedSubTasks.map { it.toEntity() })
        }
    }

    fun toggleExpansion(groupId: UUID) = viewModelScope.launch {
        _taskGroups.value.find { it.id == groupId }?.let { group ->
            val updatedGroup = group.copy(isExpanded = !group.isExpanded)
            repository.updateTaskGroup(updatedGroup.toEntity())
        }
    }

    fun moveTask(fromIndexInTodo: Int, toIndexInTodo: Int) {
        _taskGroups.update { currentTasks ->
            val todoTasks = currentTasks.filter { !it.isCompleted }.toMutableList()
            val completedTasks = currentTasks.filter { it.isCompleted }

            if (fromIndexInTodo !in todoTasks.indices) {
                return@update currentTasks
            }

            val itemToMove = todoTasks.removeAt(fromIndexInTodo)

            val targetIndex = toIndexInTodo.coerceIn(0, todoTasks.size)
            todoTasks.add(targetIndex, itemToMove)

            todoTasks + completedTasks
        }
    }

    fun saveTaskOrder() {
        viewModelScope.launch {
            val todoTasks = _taskGroups.value.filter { !it.isCompleted }.mapIndexed { index, taskGroup ->
                taskGroup.copy(order = index)
            }
            val completedTasks = _taskGroups.value.filter { it.isCompleted }.mapIndexed { index, taskGroup ->
                taskGroup.copy(order = todoTasks.size + index)
            }
            repository.updateTaskOrder(todoTasks.map { it.toEntity() } + completedTasks.map { it.toEntity() })
        }
    }

    fun upsertTask(taskToSave: TaskGroup) = viewModelScope.launch {
        val existingTask = _taskGroups.value.any { it.id == taskToSave.id }
        if (existingTask) {
            repository.updateTask(taskToSave.toEntity(), taskToSave.subTasks.map { it.toEntity() })
        } else {
            repository.insertTask(taskToSave.toEntity(), taskToSave.subTasks.map { it.toEntity() })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDueDateStatus(dueDate: LocalDateTime?): DueDateStatus? {
        if (dueDate == null) return null
        val now = LocalDateTime.now()
        return when {
            dueDate.isBefore(now) -> DueDateStatus.OVERDUE
            dueDate.isBefore(now.plusDays(2)) -> DueDateStatus.UPCOMING
            else -> DueDateStatus.LONG_TERM
        }
    }
}
