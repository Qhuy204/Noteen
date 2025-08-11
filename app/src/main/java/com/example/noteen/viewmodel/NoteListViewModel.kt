package com.example.noteen.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.LocalRepository.reposity.FolderRepository
import com.example.noteen.data.LocalRepository.reposity.NoteRepository
import com.example.noteen.data.model.FolderTag
import com.example.noteen.utils.json
import com.example.noteen.utils.json1
import com.example.noteen.utils.json2
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository: NoteRepository
    private val folderRepository: FolderRepository


    private val _id = MutableStateFlow(0)
    val id: StateFlow<Int> = _id.asStateFlow()

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    suspend fun getSelectedNote(id: Int): NoteEntity? {
        val note = noteRepository.getNoteById(id)
        _selectedNote.value = note
        return note
    }
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
            loadNotes()
            loadFolderTags()
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            _selectedNote.value?.let {
                noteRepository.deleteNoteById(it.id)
                loadNotes()
                loadFolderTags()
            }
        }
    }

    private val _showOverlay = MutableStateFlow(false)
    val showOverlay: StateFlow<Boolean> = _showOverlay.asStateFlow()

    fun setId(newId: Int) {
        Log.d("TextEditor", "ID: $newId")
        _id.value = newId
        viewModelScope.launch {
            if (newId != 0) {
                TextEditorEngine.reset()
                getSelectedNote(newId)
                _selectedNote.value?.let {
                    TextEditorEngine.setContent(it.name, it.content)
                    Log.d("TextEditor", "Selected note: ${it.id}")
                }
//                TextEditorEngine.setContent("Hello", listOf(json1, json2, json).random())
                delay(300)
                _showOverlay.value = true
            } else {
                _showOverlay.value = false
            }
        }
    }
    fun saveEditorData(title: String, json: String, plaintext: String) {
        if (title.trim() == "" && (json == "" || json == "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}")) {
            deleteNote()
            loadNotes()
            Log.d("TextEditor", "Deleted")
            return
        }

        _selectedNote.value?.let {

            if (title != it.name || json != it.content) {
                updateNote(it.copy(name = title, content = json, plaintext = plaintext, updatedAt = System.currentTimeMillis()))
                Log.d("TextEditor", "Updated id ${it.id}")
            }
        }
    }

    fun saveDrawingNote(content: String, thumbnail: String) {
        if (content == "{\"strokes\":[]}") {
            deleteNote()
        }
        _selectedNote.value?.let {
            if (content != it.content) {
                updateNote(it.copy(content = content, thumbnail = thumbnail, updatedAt = System.currentTimeMillis()))
                Log.d("TextEditor", "Updated id ${it.id}")
            }
        }
    }

    fun hideOverlay() {
        _showOverlay.value = false
    }


    private val _folderTags = MutableStateFlow<List<FolderTag>>(emptyList())
    val folderTags: StateFlow<List<FolderTag>> = _folderTags.asStateFlow()

    private val _selectedFolderName = MutableStateFlow("All")
    val selectedFolderName = _selectedFolderName.asStateFlow()

    fun selectFolder(name: String) {
        _selectedFolderName.value = name
        loadNotes()
    }

    fun loadFolderTags() {
        viewModelScope.launch {
            _folderTags.value = folderRepository.getFolderTagsWithCounts()
        }
    }

    fun addFolder(name: String, description: String) {
        viewModelScope.launch {
            val folder = FolderEntity(name = name, description = description)
            folderRepository.insertFolder(folder)
            loadFolderTags()
            selectFolder(name)
        }
    }

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGridLayout = MutableStateFlow(true)
    val isGridLayout = _isGridLayout.asStateFlow()

    fun toggleLayout() {
        _isGridLayout.value = !_isGridLayout.value
    }

    private val _sortMode = MutableStateFlow(0)
    val sortMode = _sortMode.asStateFlow()

    fun setSortMode(mode: Int) {
        _sortMode.value = mode
        loadNotes()
    }

    init {
        val db = AppDatabase.getInstance(application)
        noteRepository = NoteRepository(db.noteDao())
        folderRepository = FolderRepository(db.folderDao())

        loadFolderTags()
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _notes.value = noteRepository.getNotesByFolderName(_selectedFolderName.value, _sortMode.value)
            _isLoading.value = false
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _notes.value = noteRepository.searchNotes(query)
            _isLoading.value = false
        }
    }

    suspend fun insertNote(note: NoteEntity): Long {
        return noteRepository.insertNote(note, _selectedFolderName.value)
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(id)
            loadNotes()
        }
    }

    fun moveNoteToFolder(noteId: Int, newFolderName: String?) {
        viewModelScope.launch {
            noteRepository.updateNoteFolder(noteId, newFolderName)
            loadNotes()
        }
    }
}
