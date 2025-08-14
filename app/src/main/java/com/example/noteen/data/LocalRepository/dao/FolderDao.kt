package com.example.noteen.data.LocalRepository.dao

import androidx.room.*
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.model.FolderTag

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

    @Query(
        """
    SELECT 'All' AS name, COUNT(*) AS count FROM notes
    WHERE is_deleted = 0
    UNION ALL
    SELECT name, count FROM (
        SELECT f.name AS name, COUNT(n.id) AS count
        FROM folders f
        LEFT JOIN notes n ON f.id = n.folder_id AND is_deleted = 0
        GROUP BY f.id
        ORDER BY f.created_at DESC
    )
    UNION ALL
    SELECT * FROM (
        SELECT 'Uncategorized' AS name, COUNT(*) AS count
        FROM notes
        WHERE folder_id IS NULL AND is_deleted = 0
    )
    WHERE EXISTS (SELECT 1 FROM folders)
    """
    )
    suspend fun getFolderTagsWithCounts(): List<FolderTag>
}
