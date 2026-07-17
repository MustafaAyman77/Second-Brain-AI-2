package com.example.data

import kotlinx.coroutines.flow.Flow

class MainRepository(private val db: AppDatabase) {
    val user: Flow<User?> = db.userDao().getUser()
    val allTasks: Flow<List<Task>> = db.taskDao().getAllTasks()
    val allProjects: Flow<List<Project>> = db.projectDao().getAllProjects()
    val allNotes: Flow<List<Note>> = db.noteDao().getAllNotes()
    val allIdeas: Flow<List<Idea>> = db.ideaDao().getAllIdeas()
    val allGoals: Flow<List<Goal>> = db.goalDao().getAllGoals()
    val allHabits: Flow<List<Habit>> = db.habitDao().getAllHabits()
    val allDocuments: Flow<List<Document>> = db.documentDao().getAllDocuments()
    val allEvents: Flow<List<CalendarEvent>> = db.calendarEventDao().getAllEvents()
    val allChats: Flow<List<ChatMessage>> = db.chatDao().getAllChats()
    val allMemory: Flow<List<AiMemory>> = db.aiMemoryDao().getAllMemory()
    val settings: Flow<Settings?> = db.settingsDao().getSettings()

    suspend fun insertUser(user: User) = db.userDao().insertUser(user)
    
    suspend fun insertTask(task: Task) = db.taskDao().insertTask(task)
    suspend fun updateTask(task: Task) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: Task) = db.taskDao().deleteTask(task)

    suspend fun insertProject(project: Project) = db.projectDao().insertProject(project)
    suspend fun updateProject(project: Project) = db.projectDao().updateProject(project)
    suspend fun deleteProject(project: Project) = db.projectDao().deleteProject(project)

    suspend fun insertNote(note: Note) = db.noteDao().insertNote(note)
    suspend fun searchNotes(query: String): Flow<List<Note>> = db.noteDao().searchNotes(query)
    suspend fun deleteNote(note: Note) = db.noteDao().deleteNote(note)

    suspend fun insertIdea(idea: Idea) = db.ideaDao().insertIdea(idea)
    suspend fun deleteIdea(idea: Idea) = db.ideaDao().deleteIdea(idea)

    suspend fun insertGoal(goal: Goal) = db.goalDao().insertGoal(goal)
    suspend fun deleteGoal(goal: Goal) = db.goalDao().deleteGoal(goal)

    suspend fun insertHabit(habit: Habit) = db.habitDao().insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = db.habitDao().updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = db.habitDao().deleteHabit(habit)

    suspend fun insertDocument(document: Document) = db.documentDao().insertDocument(document)
    suspend fun deleteDocument(document: Document) = db.documentDao().deleteDocument(document)

    suspend fun insertEvent(event: CalendarEvent) = db.calendarEventDao().insertEvent(event)
    suspend fun deleteEvent(event: CalendarEvent) = db.calendarEventDao().deleteEvent(event)

    suspend fun insertChat(chat: ChatMessage) = db.chatDao().insertChat(chat)
    suspend fun clearChats() = db.chatDao().clearChats()

    suspend fun insertMemory(memory: AiMemory) = db.aiMemoryDao().insertMemory(memory)
    suspend fun deleteMemory(memory: AiMemory) = db.aiMemoryDao().deleteMemory(memory)

    suspend fun insertSettings(settings: Settings) = db.settingsDao().insertSettings(settings)
}
