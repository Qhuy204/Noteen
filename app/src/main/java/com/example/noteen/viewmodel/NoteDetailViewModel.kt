package com.example.noteen.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteen.TextEditorEngine
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.LocalRepository.reposity.NoteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val noteRepository = NoteRepository(AppDatabase.getInstance(application).noteDao())

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    suspend fun getSelectedNote(id: Int): NoteEntity? {
        val note = noteRepository.getNoteById(id)
        return note
    }

    fun loadNote(note: NoteEntity) {
        TextEditorEngine.reset()
        _selectedNote.value = note
        _selectedNote.value?.let {
            TextEditorEngine.setContent(it.name, it.content)
            Log.d("TextEditor", "Selected note: ${it.content}")
        }
        Log.i("vminit", "New note id: ${_selectedNote.value!!.id}")
    }

    suspend fun updateNote(note: NoteEntity) {
        noteRepository.updateNote(note)
    }
    
    suspend fun deleteNote() {
        _selectedNote.value?.let {
            noteRepository.deleteNoteById(it.id)
        }
    }

    suspend fun saveTextEditorData(title: String, json: String, plaintext: String) {
        if (title.trim() == "" && (json == "" || json == "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}")) {
            deleteNote()
            return
        }
        _selectedNote.value?.let {
            if (title != it.name || json != it.content) {
                updateNote(it.copy(name = title, content = json, plaintext = plaintext, updatedAt = System.currentTimeMillis()))
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
            }
        }
    }

    suspend fun saveTextNote() {
        val (title, json, plain) = TextEditorEngine.waitForContentUpdate()
        saveTextEditorData(title, json, plain)
    }

    suspend fun softDeleteNote() {
        _selectedNote.value?.let {
            noteRepository.softDeleteNoteById(it.id)
        }
        _selectedNote.value = null
    }
}
