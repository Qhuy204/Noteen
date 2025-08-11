package com.example.noteen.data.LocalRepository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folder_id")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val name: String = "",

    val type: String,

    @ColumnInfo(name = "folder_id")
    val folderId: Int? = null,

    val content: String = "",

    val plaintext: String = "",

    val thumbnail: String = "",

    val color: String = "#FFFFFF",

    val background: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)