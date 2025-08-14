package com.example.noteen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.NoteEntity
import com.example.noteen.data.LocalRepository.reposity.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    init {
        val dao = AppDatabase.getInstance(application).noteDao()
        repository = NoteRepository(dao)
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notes.value = repository.getDeletedNotes()
        }
    }

    fun restoreNotes(ids: List<Int>) {
        viewModelScope.launch {
            ids.forEach { id ->
                repository.restoreNote(id)
            }
            _notes.value = repository.getDeletedNotes()
        }
    }

    fun deleteNotes(ids: List<Int>) {
        viewModelScope.launch {
            ids.forEach { id ->
                repository.deleteNoteById(id)
            }
            _notes.value = repository.getDeletedNotes()
        }
    }
}