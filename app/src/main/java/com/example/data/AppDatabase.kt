package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Task::class,
        Project::class,
        Note::class,
        Idea::class,
        Goal::class,
        Habit::class,
        Document::class,
        CalendarEvent::class,
        ChatMessage::class,
        AiMemory::class,
        Settings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun noteDao(): NoteDao
    abstract fun ideaDao(): IdeaDao
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun documentDao(): DocumentDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun chatDao(): ChatDao
    abstract fun aiMemoryDao(): AiMemoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "second_brain_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
