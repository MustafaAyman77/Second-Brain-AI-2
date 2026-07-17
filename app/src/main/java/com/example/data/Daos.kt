package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY timestamp DESC")
    fun getAllIdeas(): Flow<List<Idea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: Idea)

    @Delete
    suspend fun deleteIdea(idea: Idea)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY timestamp DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY eventTime ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatMessage)

    @Query("DELETE FROM chats")
    suspend fun clearChats()
}

@Dao
interface AiMemoryDao {
    @Query("SELECT * FROM ai_memory ORDER BY timestamp DESC")
    fun getAllMemory(): Flow<List<AiMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AiMemory)

    @Delete
    suspend fun deleteMemory(memory: AiMemory)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)
}
