package com.example.data

import kotlinx.coroutines.flow.Flow

class MainRepository(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val noteDao: NoteDao,
    private val goalDao: GoalDao,
    private val habitDao: HabitDao,
    private val calendarEventDao: CalendarEventDao,
    private val aiMemoryDao: AIMemoryDao,
    private val chatDao: ChatDao,
    private val documentDao: DocumentDao
) {
    // --- Tasks ---
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    fun getTasksByProject(projectId: Int): Flow<List<Task>> = taskDao.getTasksByProject(projectId)
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    // --- Projects ---
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    suspend fun insertProject(project: Project): Long = projectDao.insertProject(project)
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)
    suspend fun getProjectById(id: Int): Project? = projectDao.getProjectById(id)

    // --- Notes ---
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes("%$query%")
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)

    // --- Goals ---
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: Goal): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    // --- Habits ---
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
    suspend fun getHabitById(id: Int): Habit? = habitDao.getHabitById(id)

    // --- Calendar Events ---
    val allEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()
    fun getEventsInRange(start: Long, end: Long): Flow<List<CalendarEvent>> = calendarEventDao.getEventsInRange(start, end)
    suspend fun insertEvent(event: CalendarEvent): Long = calendarEventDao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEvent) = calendarEventDao.updateEvent(event)
    suspend fun deleteEvent(event: CalendarEvent) = calendarEventDao.deleteEvent(event)

    // --- AI Memory ---
    val allMemories: Flow<List<AIMemory>> = aiMemoryDao.getAllMemories()
    fun getMemoriesByCategory(category: String): Flow<List<AIMemory>> = aiMemoryDao.getMemoriesByCategory(category)
    suspend fun insertMemory(memory: AIMemory): Long = aiMemoryDao.insertMemory(memory)
    suspend fun updateMemory(memory: AIMemory) = aiMemoryDao.updateMemory(memory)
    suspend fun deleteMemoryById(id: Int) = aiMemoryDao.deleteById(id)

    // --- Chat Sessions ---
    val allChatSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> = chatDao.getMessagesForSession(sessionId)
    suspend fun insertSession(session: ChatSession): Long = chatDao.insertSession(session)
    suspend fun insertMessage(message: ChatMessage): Long = chatDao.insertMessage(message)
    suspend fun deleteSession(sessionId: Int) = chatDao.deleteSession(sessionId)
    suspend fun deleteMessagesForSession(sessionId: Int) = chatDao.deleteMessagesForSession(sessionId)

    // --- Documents ---
    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    suspend fun insertDocument(document: Document): Long = documentDao.insertDocument(document)
    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)
    suspend fun getDocumentById(id: Int): Document? = documentDao.getDocumentById(id)
}
