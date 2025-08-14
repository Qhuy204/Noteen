package com.example.noteen.data.LocalRepository.dao

import androidx.room.*
import com.example.noteen.data.LocalRepository.entity.NoteEntity

@Dao
interface NoteDao {
    @Query("""
    INSERT INTO notes (
        name, type, folder_id, content, plaintext, thumbnail,
        color, background, pinned_at, is_locked, is_deleted,
        created_at, updated_at
    )
    VALUES (
        :name,
        :type,
        CASE
            WHEN :folderName IS NULL OR NOT EXISTS (
                SELECT 1 FROM folders WHERE name = :folderName
            ) THEN NULL
            ELSE (SELECT id FROM folders WHERE name = :folderName LIMIT 1)
        END,
        :content,
        :plaintext,
        :thumbnail,
        :color,
        :background,
        :pinnedAt,
        :isLocked,
        :isDeleted,
        :createdAt,
        :updatedAt
    )
""")
    suspend fun insertNoteWithFolderNameRaw(
        name: String,
        type: String,
        content: String,
        plaintext: String,
        thumbnail: String? = "",
        color: String = "#FFFFFF",
        background: String = "",
        pinnedAt: Long? = null,
        isLocked: Boolean = false,
        isDeleted: Boolean = false,
        createdAt: Long,
        updatedAt: Long,
        folderName: String?
    )

    @Query("SELECT last_insert_rowid()")
    suspend fun getLastInsertedNoteId(): Long

    @Transaction
    suspend fun insertNoteWithFolderName(note: NoteEntity, folderName: String?): Long {
        insertNoteWithFolderNameRaw(
            name = note.name,
            type = note.type,
            content = note.content,
            plaintext = note.plaintext,
            thumbnail = note.thumbnail,
            color = note.color,
            background = note.background,
            pinnedAt = note.pinnedAt,
            isLocked = note.isLocked,
            isDeleted = note.isDeleted,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            folderName = folderName
        )
        return getLastInsertedNoteId()
    }

    @Update suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE notes SET is_deleted = 1 WHERE id = :id")
    suspend fun softDeleteById(id: Int)

    @Query("UPDATE notes SET is_deleted = 0 WHERE id = :noteId")
    suspend fun restore(noteId: Int)

    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY updated_at DESC")
    suspend fun getAllDeletedNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id AND is_deleted = 0 LIMIT 1")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Query("""
    UPDATE notes
    SET folder_id = (
        SELECT id FROM folders WHERE name = :newFolderName LIMIT 1
    ),
    updated_at = :updatedAt
    WHERE id = :noteId
""")
    suspend fun updateFolderByName(
        noteId: Int,
        newFolderName: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
    SELECT * FROM notes
    WHERE (name LIKE '%' || :query || '%' OR plaintext LIKE '%' || :query || '%')
      AND is_deleted = 0
    ORDER BY updated_at DESC
""")
    suspend fun searchNotes(query: String): List<NoteEntity>

    // Pin / Unpin note
    @Query("""
    UPDATE notes
    SET pinned_at = CASE 
                        WHEN :pinned THEN strftime('%s','now') 
                        ELSE NULL 
                    END
    WHERE id = :noteId
""")
    suspend fun setPinned(noteId: Int, pinned: Boolean)

    // Lock / Unlock note
    @Query("""
    UPDATE notes
    SET is_locked = :locked
    WHERE id = :noteId
""")
    suspend fun setLocked(noteId: Int, locked: Boolean)



    @Query("""
    SELECT * FROM notes
    WHERE is_deleted = 0
    ORDER BY pinned_at DESC, updated_at DESC
""")
    suspend fun getAllNotesSortedByUpdated(): List<NoteEntity>

    @Query("""
    SELECT * FROM notes
    WHERE is_deleted = 0
    ORDER BY pinned_at DESC, created_at DESC
""")
    suspend fun getAllNotesSortedByCreated(): List<NoteEntity>

    @Query("""
    SELECT * FROM notes
    WHERE is_deleted = 0
    ORDER BY pinned_at DESC, name COLLATE NOCASE ASC
""")
    suspend fun getAllNotesSortedByName(): List<NoteEntity>


    @Query("""
    SELECT * FROM notes
    WHERE folder_id IS NULL AND is_deleted = 0
    ORDER BY pinned_at DESC, updated_at DESC
""")
    suspend fun getUncategorizedNotesSortedByUpdated(): List<NoteEntity>

    @Query("""
    SELECT * FROM notes
    WHERE folder_id IS NULL AND is_deleted = 0
    ORDER BY pinned_at DESC, created_at DESC
""")
    suspend fun getUncategorizedNotesSortedByCreated(): List<NoteEntity>

    @Query("""
    SELECT * FROM notes
    WHERE folder_id IS NULL AND is_deleted = 0
    ORDER BY pinned_at DESC, name COLLATE NOCASE ASC
""")
    suspend fun getUncategorizedNotesSortedByName(): List<NoteEntity>


    @Query("""
    SELECT notes.* FROM notes
    INNER JOIN folders ON notes.folder_id = folders.id
    WHERE folders.name = :folderName AND notes.is_deleted = 0
    ORDER BY notes.pinned_at DESC, notes.updated_at DESC
""")
    suspend fun getNotesByFolderNameSortedByUpdated(folderName: String): List<NoteEntity>

    @Query("""
    SELECT notes.* FROM notes
    INNER JOIN folders ON notes.folder_id = folders.id
    WHERE folders.name = :folderName AND notes.is_deleted = 0
    ORDER BY notes.pinned_at DESC, notes.created_at DESC
""")
    suspend fun getNotesByFolderNameSortedByCreated(folderName: String): List<NoteEntity>

    @Query("""
    SELECT notes.* FROM notes
    INNER JOIN folders ON notes.folder_id = folders.id
    WHERE folders.name = :folderName AND notes.is_deleted = 0
    ORDER BY notes.pinned_at DESC, notes.name COLLATE NOCASE ASC
""")
    suspend fun getNotesByFolderNameSortedByName(folderName: String): List<NoteEntity>
}
