package com.example.noteen.data.LocalRepository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.noteen.data.LocalRepository.dao.FolderDao
import com.example.noteen.data.LocalRepository.dao.NoteDao
import com.example.noteen.data.LocalRepository.entity.FolderEntity
import com.example.noteen.data.LocalRepository.entity.NoteEntity

@Database(entities = [FolderEntity::class, NoteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteen_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
