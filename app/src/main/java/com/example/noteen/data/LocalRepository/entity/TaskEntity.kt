package com.example.noteen.data.LocalRepository.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(tableName = "task_groups")
data class TaskGroupEntity(
    @PrimaryKey val id: UUID,
    val title: String,
    val dueDate: Long?,
    val repeatMode: Int,
    val reminder: String,
    var isExpanded: Boolean = true,
    var isCompleted: Boolean = false,
    @ColumnInfo(name = "display_order")
    var order: Int
)

@Entity(
    tableName = "sub_tasks",
    foreignKeys = [ForeignKey(
        entity = TaskGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskGroupId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SubTaskEntity(
    @PrimaryKey val id: UUID,
    val taskGroupId: UUID,
    val title: String,
    var isCompleted: Boolean,
    @ColumnInfo(name = "display_order")
    var order: Int
)

data class TaskGroupWithSubTasks(
    @Embedded val taskGroup: TaskGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskGroupId"
    )
    val subTasks: List<SubTaskEntity>
)
