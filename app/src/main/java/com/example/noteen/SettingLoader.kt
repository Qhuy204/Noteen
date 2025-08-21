package com.example.noteen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.SupervisorJob
import java.io.File

object SettingLoader {

    private val NOTES_GRID_KEY = booleanPreferencesKey("notes_isGridLayout")
    private val NOTES_SORT_KEY = intPreferencesKey("notes_sortMode")
    private val FOLDERS_GRID_KEY = booleanPreferencesKey("folders_isGridLayout")
    private val FOLDERS_SORT_KEY = intPreferencesKey("folders_sortMode")
    private val CURRENT_FOLDER_KEY = stringPreferencesKey("current_folder")

    var notesIsGridLayout by mutableStateOf(true)
        private set
    var notesSortMode by mutableStateOf(0)
        private set
    var foldersIsGridLayout by mutableStateOf(true)
        private set
    var foldersSortMode by mutableStateOf(0)
        private set
    var currentFolder by mutableStateOf("All")
        private set

    private lateinit var dataStore: DataStore<Preferences>

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun init(context: Context) {
        if (!::dataStore.isInitialized) {
            dataStore = PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                scope = ioScope,
                produceFile = { File(context.applicationContext.filesDir, "app_settings.preferences_pb") }
            )
            ioScope.launch { loadInitialValues() }
        }
    }

    private suspend fun loadInitialValues() {
        val prefs = dataStore.data.first()

        notesIsGridLayout = prefs[NOTES_GRID_KEY] ?: true
        notesSortMode = prefs[NOTES_SORT_KEY] ?: 0
        foldersIsGridLayout = prefs[FOLDERS_GRID_KEY] ?: true
        foldersSortMode = prefs[FOLDERS_SORT_KEY] ?: 0

        val folder = prefs[CURRENT_FOLDER_KEY] ?: "All"
        currentFolder = if (folder == "Locked") {
            ioScope.launch { dataStore.edit { it[CURRENT_FOLDER_KEY] = "All" } }
            "All"
        } else {
            folder
        }
    }


    fun updateNotesIsGridLayout(value: Boolean) {
        notesIsGridLayout = value
        ioScope.launch { dataStore.edit { it[NOTES_GRID_KEY] = value } }
    }

    fun updateNotesSortMode(value: Int) {
        notesSortMode = value
        ioScope.launch { dataStore.edit { it[NOTES_SORT_KEY] = value } }
    }

    fun updateFoldersIsGridLayout(value: Boolean) {
        foldersIsGridLayout = value
        ioScope.launch { dataStore.edit { it[FOLDERS_GRID_KEY] = value } }
    }

    fun updateFoldersSortMode(value: Int) {
        foldersSortMode = value
        ioScope.launch { dataStore.edit { it[FOLDERS_SORT_KEY] = value } }
    }

    fun updateCurrentFolder(value: String) {
        currentFolder = value
        ioScope.launch { dataStore.edit { it[CURRENT_FOLDER_KEY] = value } }
    }
}
