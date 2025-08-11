package com.example.noteen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.reposity.FolderRepository
import com.example.noteen.data.model.FolderTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FolderRepository

    private val _folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folders: StateFlow<List<FolderEntity>> = _folders.asStateFlow()

    private val _sortMode = MutableStateFlow(0)
    val sortMode: StateFlow<Int> = _sortMode.asStateFlow()

    var currentSortMode: Int
        get() = _sortMode.value
        set(value) {
            _sortMode.value = value
            loadFolders(value)
        }

    private val _selectedFolder = MutableStateFlow<FolderEntity?>(null)
    val selectedFolder: StateFlow<FolderEntity?> = _selectedFolder.asStateFlow()

    fun selectFolder(folderId: Int) {
        viewModelScope.launch {
            val folder = repository.getFolderById(folderId)
            _selectedFolder.value = folder
        }
    }

    fun clearSelectedFolder() {
        _selectedFolder.value = null
    }

    private val _folderTags = MutableStateFlow<List<FolderTag>>(emptyList())
    val folderTags: StateFlow<List<FolderTag>> = _folderTags.asStateFlow()

    fun loadFolderTags() {
        viewModelScope.launch {
            _folderTags.value = repository.getFolderTagsWithCounts()
        }
    }

    init {
        val dao = AppDatabase.getInstance(application).folderDao()
        repository = FolderRepository(dao)
        loadFolders()
        loadFolderTags()
    }

    fun loadFolders(mode: Int = _sortMode.value) {
        viewModelScope.launch {
            _sortMode.value = mode
            val data = repository.getAllFolders(mode)
            _folders.value = data
        }
    }

    fun addFolder(name: String, description: String) {
        viewModelScope.launch {
            val folder = FolderEntity(name = name, description = description)
            repository.insertFolder(folder)
            loadFolders()
            loadFolderTags()
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
            loadFolders()
            loadFolderTags()
        }
    }

    fun deleteFolderById(folderId: Int) {
        viewModelScope.launch {
            repository.deleteFolderById(folderId)
            loadFolders()
            loadFolderTags()
        }
    }

    fun lockFolder(folderId: Int, locked: Boolean) {
        viewModelScope.launch {
            repository.lockFolder(folderId, locked)
            loadFolders()
            loadFolderTags()
        }
    }

    fun searchFolders(query: String) {
        viewModelScope.launch {
            val result = repository.searchFolders(query)
            _folders.value = result
        }
    }
}
