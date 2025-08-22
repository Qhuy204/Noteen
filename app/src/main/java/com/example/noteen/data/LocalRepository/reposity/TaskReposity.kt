package com.example.noteen.data.LocalRepository.reposity

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.noteen.data.LocalRepository.dao.TaskDao
import com.example.noteen.data.LocalRepository.entity.SubTaskEntity
import com.example.noteen.data.LocalRepository.entity.TaskGroupEntity
import com.example.noteen.data.LocalRepository.entity.TaskGroupWithSubTasks
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {

    fun getTaskGroupsWithSubTasks(): Flow<List<TaskGroupWithSubTasks>> {
        return taskDao.getTaskGroupsWithSubTasks()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNearestUpcomingTask(): Flow<TaskGroupWithSubTasks?> {
        val now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        return taskDao.getNearestUpcomingTask(now)
    }

    suspend fun insertTask(taskGroup: TaskGroupEntity, subTasks: List<SubTaskEntity>) {
        taskDao.saveNewTask(taskGroup, subTasks)
    }

    suspend fun updateTask(taskGroup: TaskGroupEntity, subTasks: List<SubTaskEntity>) {
        taskDao.updateTask(taskGroup, subTasks)
    }

    suspend fun updateTaskGroup(taskGroup: TaskGroupEntity) {
        taskDao.updateTaskGroup(taskGroup)
    }

    suspend fun updateSubTask(subTask: SubTaskEntity) {
        taskDao.updateSubTask(subTask)
    }

    suspend fun deleteTasks(ids: List<UUID>) {
        taskDao.deleteTasksByIds(ids)
    }

    suspend fun updateTaskOrder(tasks: List<TaskGroupEntity>) {
        tasks.forEach { taskDao.updateTaskGroup(it) }
    }

    suspend fun updateSubTaskOrder(subTasks: List<SubTaskEntity>) {
        taskDao.updateSubTasks(subTasks)
    }
}
