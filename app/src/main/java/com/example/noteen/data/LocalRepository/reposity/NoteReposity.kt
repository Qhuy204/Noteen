package com.example.noteen.data.LocalRepository.reposity

import com.example.noteen.data.LocalRepository.dao.NoteDao
import com.example.noteen.data.LocalRepository.entity.NoteEntity

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun insertNote(note: NoteEntity, folderName: String?): Long {
        return noteDao.insertNoteWithFolderName(note, folderName)
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.update(note)
    }

    suspend fun deleteNoteById(id: Int) {
        noteDao.deleteById(id)
    }

    suspend fun softDeleteNoteById(id: Int) {
        noteDao.softDeleteById(id)
    }

    suspend fun restoreNote(id: Int) {
        noteDao.restore(id)
    }

    suspend fun getDeletedNotes(): List<NoteEntity> {
        return noteDao.getAllDeletedNotes()
    }

    suspend fun getNoteById(id: Int): NoteEntity? {
        return noteDao.getNoteById(id)
    }

    suspend fun setNotePinned(noteId: Int, pinned: Boolean) {
        val pinnedAt: Long? = if (pinned) System.currentTimeMillis() else null
        noteDao.setPinned(noteId, pinnedAt)
    }

    suspend fun setNoteLocked(noteId: Int, locked: Boolean) {
        noteDao.setLocked(noteId, locked)
    }

    suspend fun setNoteColor(noteId: Int, color: String) {
        noteDao.setColor(noteId, color)
    }

    suspend fun setNoteBackground(noteId: Int, background: String) {
        noteDao.setBackground(noteId, background)
    }

    suspend fun searchNotes(query: String): List<NoteEntity> {
        return noteDao.searchNotes(query)
    }

    suspend fun updateNoteFolder(noteId: Int, newFolderName: String?) {
        noteDao.updateFolderByName(noteId, newFolderName)
    }

    suspend fun getNotesByFolderName(folderName: String, sortMode: Int): List<NoteEntity> {
        return when (folderName) {
            "All" -> when (sortMode) {
                0 -> noteDao.getAllNotesSortedByUpdated()
                1 -> noteDao.getAllNotesSortedByCreated()
                2 -> noteDao.getAllNotesSortedByName()
                else -> noteDao.getAllNotesSortedByUpdated()
            }
            "Uncategorized" -> when (sortMode) {
                0 -> noteDao.getUncategorizedNotesSortedByUpdated()
                1 -> noteDao.getUncategorizedNotesSortedByCreated()
                2 -> noteDao.getUncategorizedNotesSortedByName()
                else -> noteDao.getUncategorizedNotesSortedByUpdated()
            }
            "Locked" -> when (sortMode) {
                0 -> noteDao.getLockedNotesSortedByUpdated()
                1 -> noteDao.getLockedNotesSortedByCreated()
                2 -> noteDao.getLockedNotesSortedByName()
                else -> noteDao.getLockedNotesSortedByUpdated()
            }
            else -> when (sortMode) {
                0 -> noteDao.getNotesByFolderNameSortedByUpdated(folderName)
                1 -> noteDao.getNotesByFolderNameSortedByCreated(folderName)
                2 -> noteDao.getNotesByFolderNameSortedByName(folderName)
                else -> noteDao.getNotesByFolderNameSortedByUpdated(folderName)
            }
        }
    }
}
