package com.example.noteen.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteen.SettingLoader
import com.example.noteen.data.LocalRepository.AppDatabase
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.reposity.FolderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FolderListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FolderRepository

    private val _folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folders: StateFlow<List<FolderEntity>> = _folders.asStateFlow()

    var isGridLayout by mutableStateOf(SettingLoader.foldersIsGridLayout)
        private set
    var sortMode by mutableStateOf(SettingLoader.foldersSortMode)
        private set

    fun updateGridLayout(value: Boolean) {
        SettingLoader.updateFoldersIsGridLayout(value)
        isGridLayout = value
    }

    fun updateSortMode(value: Int) {
        SettingLoader.updateFoldersSortMode(value)
        sortMode = value
        loadFolders()
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

    init {
        val dao = AppDatabase.getInstance(application).folderDao()
        repository = FolderRepository(dao)
    }


    fun loadFolders() {
        viewModelScope.launch {
            val data = repository.getAllFolders(sortMode)
            _folders.value = data
        }
    }

    fun addFolder(name: String, description: String) {
        viewModelScope.launch {
            val folder = FolderEntity(name = name, description = description)
            repository.insertFolder(folder)
            loadFolders()
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
            loadFolders()
        }
    }

    fun deleteFolderById(folderId: Int) {
        viewModelScope.launch {
            repository.deleteFolderById(folderId)
            loadFolders()
        }
        SettingLoader.updateCurrentFolder("All")
    }

    fun lockFolder(folderId: Int, locked: Boolean) {
        viewModelScope.launch {
            repository.lockFolder(folderId, locked)
            loadFolders()
        }
    }

    fun searchFolders(query: String) {
        viewModelScope.launch {
            val result = repository.searchFolders(query)
            _folders.value = result
        }
    }
}
