package com.example.noteen.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.SettingLoader
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.LocalRepository.reposity.FolderRepository
import com.example.noteen.data.LocalRepository.reposity.NoteRepository
import com.example.noteen.data.LocalRepository.model.FolderTag
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteListViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository: NoteRepository
    private val folderRepository: FolderRepository

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    suspend fun getSelectedNote(id: Int): NoteEntity? {
        val note = noteRepository.getNoteById(id)
        return note
    }

    fun selectNote(note: NoteEntity?) {
        viewModelScope.launch {
            if (note != null) {
                _selectedNote.value = note
                delay(200)
                _showOverlay.value = true
            }
            else {
                _showOverlay.value = false
            }
        }
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

    fun hideOverlay() {
        _showOverlay.value = false
        loadNotes()
        loadFolderTags()
    }


    private val _folderTags = MutableStateFlow<List<FolderTag>>(emptyList())
    val folderTags: StateFlow<List<FolderTag>> = _folderTags.asStateFlow()

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
            updateCurrentFolder(name)
        }
    }

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var currentFolder by mutableStateOf(SettingLoader.currentFolder)
        private set

    fun updateCurrentFolder(folderName: String) {
        SettingLoader.updateCurrentFolder(folderName)
        Log.i("testtt", "Folder current: $folderName")
        currentFolder = folderName
        loadNotes()
    }

    var isGridLayout by mutableStateOf(SettingLoader.notesIsGridLayout)
        private set
    var sortMode by mutableStateOf(SettingLoader.notesSortMode)
        private set

    fun updateGridLayout(value: Boolean) {
        SettingLoader.updateNotesIsGridLayout(value)
        isGridLayout = value
    }

    fun updateSortMode(value: Int) {
        SettingLoader.updateNotesSortMode(value)
        sortMode = value
        loadNotes()
    }

    fun loadData() {
        loadFolderTags()
        currentFolder = SettingLoader.currentFolder
        loadNotes()
    }

    init {
        val db = AppDatabase.getInstance(application)
        noteRepository = NoteRepository(db.noteDao())
        folderRepository = FolderRepository(db.folderDao())

        loadData()

        Log.i("vminit","Notes init")
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _notes.value = noteRepository.getNotesByFolderName(currentFolder, sortMode)
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
        return noteRepository.insertNote(note, currentFolder)
    }

    fun softDeleteNote(id: Int) {
        viewModelScope.launch {
            noteRepository.softDeleteNoteById(id)
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
