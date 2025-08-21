package com.example.noteen.data.LocalRepository.dao

import androidx.room.*
import com.example.noteen.data.LocalRepository.entity.SubTaskEntity
import com.example.noteen.data.LocalRepository.entity.TaskGroupEntity
import com.example.noteen.data.LocalRepository.entity.TaskGroupWithSubTasks
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM task_groups ORDER BY display_order ASC")
    fun getTaskGroupsWithSubTasks(): Flow<List<TaskGroupWithSubTasks>>

    @Transaction
    @Query("SELECT * FROM task_groups WHERE isCompleted = 0 AND dueDate IS NOT NULL AND dueDate > :currentTime ORDER BY dueDate ASC LIMIT 1")
    fun getNearestUpcomingTask(currentTime: Long): Flow<TaskGroupWithSubTasks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskGroup(taskGroup: TaskGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>)

    @Update
    suspend fun updateTaskGroup(taskGroup: TaskGroupEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTasks(subTasks: List<SubTaskEntity>)

    @Query("DELETE FROM task_groups WHERE id IN (:ids)")
    suspend fun deleteTasksByIds(ids: List<UUID>)

    @Query("DELETE FROM sub_tasks WHERE id = :subTaskId")
    suspend fun deleteSubTaskById(subTaskId: UUID)

    @Transaction
    suspend fun saveNewTask(taskGroup: TaskGroupEntity, subTasks: List<SubTaskEntity>) {
        insertTaskGroup(taskGroup)
        insertSubTasks(subTasks)
    }

    @Transaction
    suspend fun updateTask(taskGroup: TaskGroupEntity, subTasks: List<SubTaskEntity>) {
        updateTaskGroup(taskGroup)
        // Xóa các subtask cũ không còn trong danh sách mới
        val oldSubTasks = getSubTasksForGroup(taskGroup.id)
        val newSubTaskIds = subTasks.map { it.id }.toSet()
        val toDelete = oldSubTasks.filterNot { it.id in newSubTaskIds }
        toDelete.forEach { deleteSubTaskById(it.id) }
        // Cập nhật hoặc thêm các subtask mới
        insertSubTasks(subTasks)
    }

    @Query("SELECT * FROM sub_tasks WHERE taskGroupId = :groupId")
    suspend fun getSubTasksForGroup(groupId: UUID): List<SubTaskEntity>

}
