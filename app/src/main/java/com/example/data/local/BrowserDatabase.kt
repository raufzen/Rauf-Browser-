package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        DownloadEntity::class,
        ShortcutEntity::class,
        ToolHistoryEntity::class,
        AiSessionEntity::class,
        AiMessageEntity::class,
        NoteEntity::class,
        TodoEntity::class,
        ReminderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun toolHistoryDao(): ToolHistoryDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun utilityDao(): UtilityDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "rauf_browser_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
