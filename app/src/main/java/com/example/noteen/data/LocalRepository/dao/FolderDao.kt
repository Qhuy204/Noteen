package com.example.noteen.data.LocalRepository.dao

import androidx.room.*
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.model.FolderTag

@Dao
interface FolderDao {
    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteById(folderId: Int)

    @Query("UPDATE folders SET is_locked = :locked, updated_at = :updatedAt WHERE id = :folderId")
    suspend fun lockFolder(folderId: Int, locked: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Int): FolderEntity?

    @Query("SELECT * FROM folders ORDER BY created_at DESC")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ORDER BY created_at DESC")
    suspend fun searchFolders(query: String): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY updated_at DESC")
    suspend fun getFoldersSortedByUpdated(): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY created_at DESC")
    suspend fun getFoldersSortedByCreated(): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    suspend fun getFoldersSortedByName(): List<FolderEntity>

    @Query("""
    SELECT * FROM (
        -- Folder ảo "All"
        SELECT 0 AS id, 'All' AS name, COUNT(*) AS count, 0 AS created_at
        FROM notes
        WHERE is_deleted = 0 AND notes.is_locked = 0

        UNION ALL

        -- Folder thật
        SELECT f.id AS id, f.name AS name, COUNT(n.id) AS count, f.created_at
        FROM folders f
        LEFT JOIN notes n ON f.id = n.folder_id AND n.is_deleted = 0 AND n.is_locked = 0
        GROUP BY f.id, f.name, f.created_at

        UNION ALL

        -- "Uncategorized" chỉ khi có ít nhất 1 folder thật
        SELECT -1 AS id, 'Uncategorized' AS name, COUNT(*) AS count, 0 AS created_at
        FROM notes
        WHERE folder_id IS NULL AND is_deleted = 0 AND notes.is_locked = 0
          AND EXISTS (SELECT 1 FROM folders)
    ) 
    ORDER BY 
        CASE WHEN name = 'All' THEN 0
             WHEN name = 'Uncategorized' THEN 2
             ELSE 1
        END,
        created_at DESC,
        name    
""")
    suspend fun getFolderTagsWithCounts(): List<FolderTag>
}
