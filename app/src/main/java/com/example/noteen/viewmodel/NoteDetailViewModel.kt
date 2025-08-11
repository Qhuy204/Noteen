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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    application: Application,
    private val noteId: Int
) : AndroidViewModel(application) {

    private val noteRepository = NoteRepository(AppDatabase.getInstance(application).noteDao())

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    fun getSelectedNote() {
        viewModelScope.launch {
            _selectedNote.value = noteRepository.getNoteById(noteId)
            _selectedNote.value?.let {
                TextEditorEngine.setContent(it.name, it.content)
                Log.i("DBTest", "Note: ${it.name}  ${it.content}  ${it.plaintext}")
                Log.i("DBTest", "Set content - title: ${TextEditorEngine.titleString.value} - content: ${TextEditorEngine.jsonContent.value} - plain: ${TextEditorEngine.plainTextContent.value}")
            }
        }
    }

    init {
        getSelectedNote()
    }

    fun saveNote() {
        TextEditorEngine.refreshContentFromWeb()

        TextEditorEngine.reset()
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
            _selectedNote.value = note
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            _selectedNote.value?.let {
                if (it.name == "" && it.content == "") noteRepository.deleteNoteById(it.id)
            }
        }
    }
}

class NoteDetailViewModelFactory(
    private val application: Application,
    private val noteId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            return NoteDetailViewModel(application, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
