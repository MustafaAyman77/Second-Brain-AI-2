package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MainRepository
    
    val user: StateFlow<User?>
    val allTasks: StateFlow<List<Task>>
    val allProjects: StateFlow<List<Project>>
    val allNotes: StateFlow<List<Note>>
    val allIdeas: StateFlow<List<Idea>>
    val allGoals: StateFlow<List<Goal>>
    val allHabits: StateFlow<List<Habit>>
    val allDocuments: StateFlow<List<Document>>
    val allEvents: StateFlow<List<CalendarEvent>>
    val allChats: StateFlow<List<ChatMessage>>
    val allMemory: StateFlow<List<AiMemory>>
    val settings: StateFlow<Settings?>

    // Pomodoro Focus Timer state
    private val _pomodoroSecondsLeft = MutableStateFlow(25 * 60)
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroSessionsCompleted = MutableStateFlow(0)
    val pomodoroSessionsCompleted: StateFlow<Int> = _pomodoroSessionsCompleted.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MainRepository(database)

        user = repository.user.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allProjects = repository.allProjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allIdeas = repository.allIdeas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allHabits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allDocuments = repository.allDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allChats = repository.allChats.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allMemory = repository.allMemory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Seed default user, settings, and habits if empty
        viewModelScope.launch {
            repository.insertUser(User(id = 1, name = "مصطفى", email = "user@example.com", plan = "FREE"))
            repository.insertSettings(Settings(id = 1, theme = "DARK", language = "AR"))
            
            // Seed a few default habits if empty
            allHabits.first().let { habits ->
                if (habits.isEmpty()) {
                    repository.insertHabit(Habit(name = "قراءة كتب ممتعة 📚", streak = 3, complianceRate = 0.8f))
                    repository.insertHabit(Habit(name = "ممارسة التأمل الهادئ 🧘‍♂️", streak = 5, complianceRate = 0.9f))
                    repository.insertHabit(Habit(name = "شرب الماء بكثرة 💧", streak = 10, complianceRate = 0.95f))
                }
            }
        }

        // Launch tick for Pomodoro timer
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_isPomodoroRunning.value) {
                    if (_pomodoroSecondsLeft.value > 0) {
                        _pomodoroSecondsLeft.value -= 1
                    } else {
                        // Finished session
                        _isPomodoroRunning.value = false
                        _pomodoroSessionsCompleted.value += 1
                        _pomodoroSecondsLeft.value = 25 * 60
                    }
                }
            }
        }
    }

    // --- Task Actions ---
    fun addTask(title: String, desc: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(Task(title = title, description = desc, priority = priority))
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Project Actions ---
    fun addProject(name: String, desc: String, progress: Float) {
        viewModelScope.launch {
            repository.insertProject(Project(name = name, description = desc, progress = progress))
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // --- Note Actions ---
    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(Note(title = title, content = content, category = category))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Idea Actions ---
    fun addIdea(title: String, description: String, category: String) {
        viewModelScope.launch {
            repository.insertIdea(Idea(title = title, description = description, category = category))
        }
    }

    fun deleteIdea(idea: Idea) {
        viewModelScope.launch {
            repository.deleteIdea(idea)
        }
    }

    fun generateAiIdeaSuggestions(idea: Idea) {
        viewModelScope.launch {
            val prompt = "أنت مساعد الأفكار المبتكر. وسّع وطوّر هذه الفكرة وقدّم نصائح عملية ومقترحات إبداعية لتنفيذها باللغة العربية بأسلوب راقٍ ومنظم ومختصر:\nالعنوان: ${idea.title}\nالوصف: ${idea.description}"
            val response = callGeminiApiDirectly(prompt)
            repository.insertIdea(idea.copy(aiSuggestions = response))
        }
    }

    // --- Goal Actions ---
    fun addGoal(title: String, period: String, target: Float) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, period = period, target = target, progress = 0f))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // --- Habit Actions ---
    fun addHabit(name: String, frequency: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name, frequency = frequency))
        }
    }

    fun checkInHabit(habit: Habit) {
        viewModelScope.launch {
            val newStreak = habit.streak + 1
            repository.updateHabit(habit.copy(
                streak = newStreak,
                complianceRate = (habit.complianceRate * 9 + 1) / 10,
                lastDone = System.currentTimeMillis()
            ))
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Calendar Actions ---
    fun addEvent(title: String, description: String, eventTime: Long, location: String) {
        viewModelScope.launch {
            repository.insertEvent(CalendarEvent(title = title, description = description, eventTime = eventTime, location = location))
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // --- Document Actions ---
    fun addDocument(name: String, mimeType: String, path: String = "") {
        viewModelScope.launch {
            val mockSummary = "تلخيص تلقائي ذكي للمستند: $name. هذا المستند يحتوي على معلومات رئيسية ونقاط تنظيمية هامة ومستخرجة بدقة."
            repository.insertDocument(Document(name = name, mimeType = mimeType, summary = mockSummary, path = path))
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // 1. Insert User message
            repository.insertChat(ChatMessage(role = "USER", message = text))
            
            // 2. Query Gemini
            val promptContext = allChats.value.takeLast(10).joinToString("\n") { "${it.role}: ${it.message}" } + "\nUSER: $text"
            val systemInstructions = "أنت Second Brain AI، العقل الثاني الذكي والمساعد الشخصي الوفي للمستخدم. تحدّث باللغة العربية الفصحى بروح إيجابية، مشجعة، راقية ومنظّمة. ساعد المستخدم في تنظيم حياته والإجابة على أي أسئلة بكل ذكاء."
            
            val aiResponse = callGeminiApiDirectly(promptContext, systemInstructions)
            
            // 3. Insert Model message
            repository.insertChat(ChatMessage(role = "MODEL", message = aiResponse))
        }
    }

    fun clearChats() {
        viewModelScope.launch {
            repository.clearChats()
        }
    }

    // --- Pomodoro timer controls ---
    fun togglePomodoro() {
        _isPomodoroRunning.value = !_isPomodoroRunning.value
    }

    fun resetPomodoro() {
        _isPomodoroRunning.value = false
        _pomodoroSecondsLeft.value = 25 * 60
    }

    // --- Call Gemini API ---
    private suspend fun callGeminiApiDirectly(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext "خطأ: لم يتم ضبط مفتاح Gemini API. يرجى إضافته من لوحة التحكم لتفعيل المساعد الذكي بالكامل. ⚠️"
        }

        val contents = listOf(Content(parts = listOf(Part(text = prompt))))
        val systemContent = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemContent,
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "لم أتمكن من معالجة هذا الطلب حالياً. يرجى المحاولة مرة أخرى."
        } catch (e: Exception) {
            "عذراً، حدث خطأ أثناء الاتصال بالخادم الذكي: ${e.localizedMessage}"
        }
    }
}
