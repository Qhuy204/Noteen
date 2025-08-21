package com.example.noteen.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteen.TextEditorEngine
import com.example.noteen.TextEditorEngine.setContent
import com.example.noteen.data.LocalFileManager.FileManager
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.LocalRepository.model.FolderTag
import com.example.noteen.data.LocalRepository.reposity.FolderRepository
import com.example.noteen.data.LocalRepository.reposity.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository = NoteRepository(AppDatabase.getInstance(application).noteDao())
    private val folderRepository = FolderRepository(AppDatabase.getInstance(application).folderDao())

    private val _folderTags = MutableStateFlow<List<FolderTag>>(emptyList())
    val folderTags: StateFlow<List<FolderTag>> = _folderTags.asStateFlow()

    fun loadFolderTags() {
        viewModelScope.launch {
            val allTags = folderRepository.getFolderTagsWithCounts()
            _folderTags.value = allTags.filter { it.id > 0 }
        }
    }

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isCategorized = MutableStateFlow(false)
    val isCategorized: StateFlow<Boolean> = _isCategorized.asStateFlow()

    private val _folder_id = MutableStateFlow(0)
    val folder_id: StateFlow<Int> = _folder_id.asStateFlow()

    suspend fun getSelectedNote(id: Int): NoteEntity? {
        val note = noteRepository.getNoteById(id)
        return note
    }

    fun loadNote(note: NoteEntity) {
        TextEditorEngine.reset()
        _selectedNote.value = note
        _selectedNote.value?.let {
            TextEditorEngine.setContent(it.name, it.content)
            _isPinned.value = note.pinnedAt != null
            _isCategorized.value = note.folderId != null
            if (note.folderId != null) _folder_id.value = note.folderId
            else _folder_id.value = 0

            _isLocked.value = it.isLocked
        }
        Log.i("vminit", "New note id: ${_selectedNote.value!!.id}")
    }

    suspend fun updateNote(note: NoteEntity) {
        noteRepository.updateNote(note)
    }
    
    suspend fun deleteNote() {
        _selectedNote.value?.let {
            noteRepository.deleteNoteById(it.id)
            FileManager.deleteFileByName(it.thumbnail)
        }
    }

    suspend fun saveTextEditorData(title: String, json: String, plaintext: String) {
        if (title.trim() == "" && (json == "" || json == "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}")) {
            deleteNote()
            return
        }
        _selectedNote.value?.let {
            if (title != it.name || json != it.content) {
                updateNote(it.copy(name = title, content = json, plaintext = plaintext))
            }
        }
    }

    suspend fun saveDrawingNote(content: String, thumbnail: String) {
        if (content == "{\"strokes\":[]}") {
            deleteNote()
        }
        _selectedNote.value?.let {
            if (content != it.content) {
                updateNote(it.copy(content = content, thumbnail = thumbnail, updatedAt = System.currentTimeMillis()))
                Log.d("TextEditor", "Updated id ${it.id}")
                FileManager.deleteFileByName(it.thumbnail)
            }
            else FileManager.deleteFileByName(thumbnail)
        }
    }

    suspend fun saveTextNote() {
        val (title, json, plain) = TextEditorEngine.waitForContentUpdate()
        saveTextEditorData(title, json, plain)
        TextEditorEngine.reset()
    }

    suspend fun softDeleteNote() {
        _selectedNote.value?.let {
            noteRepository.softDeleteNoteById(it.id)
        }
        _selectedNote.value = null
    }

    fun togglePin() {
        val newPinned = !_isPinned.value
        _isPinned.value = newPinned
        viewModelScope.launch {
            _selectedNote.value?.id?.let { noteId ->
                noteRepository.setNotePinned(noteId, newPinned)
            }
        }
    }

    suspend fun toggleLock() {
        _selectedNote.value?.let {
            noteRepository.setNoteLocked(it.id, !_isLocked.value)
        }
        _selectedNote.value = null
    }

    fun uncategorize() {
        _isCategorized.value = false
        _folder_id.value = 0
        viewModelScope.launch {
            _selectedNote.value?.id?.let { noteId ->
                noteRepository.updateNoteFolder(noteId, null)
            }
        }
    }

    fun addToNewFolder(newFolderId: Int, newFolderName: String) {
        _isCategorized.value = true
        _folder_id.value = newFolderId
        viewModelScope.launch {
            _selectedNote.value?.id?.let { noteId ->
                noteRepository.updateNoteFolder(noteId, newFolderName)
            }
        }
    }
}
