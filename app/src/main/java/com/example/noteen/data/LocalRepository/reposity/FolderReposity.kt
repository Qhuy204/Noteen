package com.example.noteen.data.LocalRepository.reposity

import com.example.noteen.data.LocalRepository.dao.FolderDao
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.model.FolderTag

class FolderRepository(private val folderDao: FolderDao) {

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insert(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)

    suspend fun deleteFolderById(folderId: Int) = folderDao.deleteById(folderId)

    suspend fun getFolderById(folderId: Int): FolderEntity? = folderDao.getFolderById(folderId)

    suspend fun lockFolder(folderId: Int, locked: Boolean) = folderDao.lockFolder(folderId, locked)

    suspend fun getAllFolders(sortMode: Int): List<FolderEntity> {
        return when (sortMode) {
            0 -> folderDao.getFoldersSortedByUpdated()
            1 -> folderDao.getFoldersSortedByCreated()
            2 -> folderDao.getFoldersSortedByName()
            else -> folderDao.getFoldersSortedByUpdated()
        }
    }

    suspend fun searchFolders(query: String): List<FolderEntity> = folderDao.searchFolders(query)

    suspend fun getFolderTagsWithCounts(): List<FolderTag> = folderDao.getFolderTagsWithCounts()
}
