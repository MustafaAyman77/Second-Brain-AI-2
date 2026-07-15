package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MainRepository(
        db.taskDao(),
        db.projectDao(),
        db.noteDao(),
        db.goalDao(),
        db.habitDao(),
        db.calendarEventDao(),
        db.aiMemoryDao(),
        db.chatDao(),
        db.documentDao()
    )

    private val sharedPrefs = application.getSharedPreferences("second_brain_prefs", Context.MODE_PRIVATE)

    // --- User Profile ---
    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "مستعمل الذكاء") ?: "مستعمل الذكاء")
    val userName = _userName.asStateFlow()

    private val _userBio = MutableStateFlow(sharedPrefs.getString("user_bio", "مطور ومفكر") ?: "مطور ومفكر")
    val userBio = _userBio.asStateFlow()

    private val _userPlan = MutableStateFlow(sharedPrefs.getString("user_plan", "Premium") ?: "Premium") // Free, Premium
    val userPlan = _userPlan.asStateFlow()

    fun updateProfile(name: String, bio: String, plan: String) {
        _userName.value = name
        _userBio.value = bio
        _userPlan.value = plan
        sharedPrefs.edit()
            .putString("user_name", name)
            .putString("user_bio", bio)
            .putString("user_plan", plan)
            .apply()
    }

    // --- Flows from Database ---
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val projects = repository.allProjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val habits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val calendarEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiMemories = repository.allMemories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val chatSessions = repository.allChatSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val documents = repository.allDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search Query ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredNotes = combine(notes, searchQuery) { notesList, query ->
        if (query.isBlank()) notesList
        else notesList.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks = combine(tasks, searchQuery) { tasksList, query ->
        if (query.isBlank()) tasksList
        else tasksList.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Chat Session State ---
    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    // Intent Action proposed by Gemini
    private val _proposedAction = MutableStateFlow<ProposedAction?>(null)
    val proposedAction = _proposedAction.asStateFlow()

    init {
        // Automatically set up a default chat session if none exists
        viewModelScope.launch {
            repository.allChatSessions.first().let { sessions ->
                if (sessions.isEmpty()) {
                    val newSessionId = repository.insertSession(ChatSession(title = "محادثة مساعد الذكاء"))
                    _currentSessionId.value = newSessionId.toInt()
                } else {
                    _currentSessionId.value = sessions.first().id
                }
            }
        }

        // Keep chat messages in sync with current session
        viewModelScope.launch {
            currentSessionId.collectLatest { sessionId ->
                if (sessionId != null) {
                    repository.getMessagesForSession(sessionId).collect { messages ->
                        _chatMessages.value = messages
                    }
                } else {
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    fun selectChatSession(id: Int) {
        _currentSessionId.value = id
        _proposedAction.value = null
    }

    fun createNewChatSession(title: String = "محادثة جديدة") {
        viewModelScope.launch {
            val newId = repository.insertSession(ChatSession(title = title))
            _currentSessionId.value = newId.toInt()
            _proposedAction.value = null
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            repository.deleteMessagesForSession(sessionId)
            repository.deleteSession(sessionId)
            repository.allChatSessions.first().let { sessions ->
                if (sessions.isNotEmpty()) {
                    _currentSessionId.value = sessions.first().id
                } else {
                    val newId = repository.insertSession(ChatSession(title = "محادثة مساعد الذكاء"))
                    _currentSessionId.value = newId.toInt()
                }
            }
        }
    }

    // --- AI Chat Logic ---
    fun sendChatMessage(text: String) {
        val sessionId = currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save User message
            repository.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = text
                )
            )

            _isChatLoading.value = true
            _proposedAction.value = null

            // Build system instruction and include existing chat context (last 8 messages)
            val history = _chatMessages.value.takeLast(8)
            val contextPrompt = history.joinToString("\n") { "${it.role}: ${it.content}" }

            val systemInstruction = """
                أنت "عقلك الثاني" (Second Brain AI) - مساعد إنتاجي شخصي ذكي وودود للغاية.
                تساعد المستخدم على تنظيم حياته وإدارتها بكفاءة عالية (المهام، الملاحظات، التقويم، المشاريع، العادات، الأهداف).
                تحدث مع المستخدم بلغة عربية فصحى مبسطة، دافئة وواضحة.
                
                عندما يطلب منك المستخدم إنشاء أو جدولة أو حفظ أي شيء بشكل صريح، قم بالرد عليه بالقبول والترحيب، ثم قم بصياغة سطر خاص في نهاية ردك يحتوي على كائن JSON يصف الإجراء المطلوب بدقة ليقوم التطبيق بتنفيذه تلقائياً فور تأكيد المستخدم.
                
                يجب كتابة سطر الإجراء بالشكل التالي تماماً وبدون أي أحرف إضافية في هذا السطر:
                [ACTION: {"action": "CREATE_TASK", "title": "عنوان المهمة", "description": "وصف المهمة", "priority": "LOW/MEDIUM/HIGH/URGENT", "dueDateOffsetDays": N}]
                أو:
                [ACTION: {"action": "CREATE_NOTE", "title": "عنوان الملاحظة", "content": "محتوى الملاحظة", "category": "التصنيف المقترح"}]
                or:
                [ACTION: {"action": "CREATE_EVENT", "title": "عنوان الحدث", "description": "الوصف", "startTimeOffsetHours": N, "durationHours": 1}]
                or:
                [ACTION: {"action": "CREATE_HABIT", "title": "اسم العادة", "frequency": "DAILY"}]
                
                حيث N هو فارق الأيام من اليوم (مثلاً: غداً N=1، بعد غد N=2، اليوم N=0).
                تأكد من صياغة الرد بشكل ملائم وجعل كائن الـ JSON مدمجاً في سطر واحد يبدأ بـ [ACTION: وينتهي بـ ].
            """.trimIndent()

            val responseText = RetrofitClient.callGemini(
                prompt = "$contextPrompt\nuser: $text",
                systemInstruction = systemInstruction,
                temperature = 0.6f
            )

            _isChatLoading.value = false

            // Extract Proposed Action if present
            val actionRegex = "\\[ACTION:\\s*(.*?)\\s*\\]".toRegex()
            val matchResult = actionRegex.find(responseText)
            var cleanText = responseText

            if (matchResult != null) {
                val jsonString = matchResult.groupValues[1]
                cleanText = responseText.replace(actionRegex, "").trim()
                try {
                    val json = JSONObject(jsonString)
                    val actionType = json.optString("action")
                    _proposedAction.value = ProposedAction(
                        type = actionType,
                        title = json.optString("title"),
                        description = json.optString("description"),
                        priority = json.optString("priority", "MEDIUM"),
                        category = json.optString("category", "عام"),
                        content = json.optString("content"),
                        offsetDays = json.optInt("dueDateOffsetDays", 0),
                        offsetHours = json.optInt("startTimeOffsetHours", 0),
                        frequency = json.optString("frequency", "DAILY")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Save Model message
            repository.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = cleanText,
                    detectedIntent = _proposedAction.value?.type
                )
            )

            // Auto Extract AI Memory Fact if the message was long and contained preferences
            extractMemoryFact(text, cleanText)
        }
    }

    fun sendVoiceChatMessage(transcribedText: String, durationSeconds: Int) {
        val sessionId = currentSessionId.value ?: return
        if (transcribedText.isBlank()) return

        viewModelScope.launch {
            // Save User message as voice note content format: [VOICE:seconds] Transcription
            repository.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = "[VOICE:$durationSeconds] $transcribedText"
                )
            )

            _isChatLoading.value = true
            _proposedAction.value = null

            // Build system instruction and include existing chat context (last 8 messages)
            val history = _chatMessages.value.takeLast(8)
            val contextPrompt = history.joinToString("\n") { "${it.role}: ${it.content}" }

            val systemInstruction = """
                أنت "عقلك الثاني" (Second Brain AI) - مساعد إنتاجي شخصي ذكي وودود للغاية.
                تساعد المستخدم على تنظيم حياته وإدارتها بكفاءة عالية (المهام، الملاحظات، التقويم، المشاريع، العادات، الأهداف).
                لقد تلقيت للتو رسالة صوتية (ريكورد) من المستخدم وتم تحويلها لنص بنجاح.
                تحدث مع المستخدم بلغة عربية فصحى مبسطة، دافئة وواضحة، وأظهر ترحيباً بالرسالة الصوتية (مثال: "لقد استمعت لرسالتك الصوتية بوضوح...").
                
                عندما يطلب منك المستخدم إنشاء أو جدولة أو حفظ أي شيء بشكل صريح، قم بالرد عليه بالقبول والترحيب، ثم قم بصياغة سطر خاص في نهاية ردك يحتوي على كائن JSON يصف الإجراء المطلوب بدقة ليقوم التطبيق بتنفيذه تلقائياً فور تأكيد المستخدم.
                
                يجب كتابة سطر الإجراء بالشكل التالي تماماً وبدون أي أحرف إضافية في هذا السطر:
                [ACTION: {"action": "CREATE_TASK", "title": "عنوان المهمة", "description": "وصف المهمة", "priority": "LOW/MEDIUM/HIGH/URGENT", "dueDateOffsetDays": N}]
                أو:
                [ACTION: {"action": "CREATE_NOTE", "title": "عنوان الملاحظة", "content": "محتوى الملاحظة", "category": "التصنيف المقترح"}]
                or:
                [ACTION: {"action": "CREATE_EVENT", "title": "عنوان الحدث", "description": "الوصف", "startTimeOffsetHours": N, "durationHours": 1}]
                or:
                [ACTION: {"action": "CREATE_HABIT", "title": "اسم العادة", "frequency": "DAILY"}]
                
                حيث N هو فارق الأيام من اليوم (مثلاً: غداً N=1، بعد غد N=2، اليوم N=0).
                تأكد من صياغة الرد بشكل ملائم وجعل كائن الـ JSON مدمجاً في سطر واحد يبدأ بـ [ACTION: وينتهي بـ ].
            """.trimIndent()

            val responseText = RetrofitClient.callGemini(
                prompt = "$contextPrompt\nuser: [رسالة صوتية مفرغة]: $transcribedText",
                systemInstruction = systemInstruction,
                temperature = 0.6f
            )

            _isChatLoading.value = false

            // Extract Proposed Action if present
            val actionRegex = "\\[ACTION:\\s*(.*?)\\s*\\]".toRegex()
            val matchResult = actionRegex.find(responseText)
            var cleanText = responseText

            if (matchResult != null) {
                val jsonString = matchResult.groupValues[1]
                cleanText = responseText.replace(actionRegex, "").trim()
                try {
                    val json = JSONObject(jsonString)
                    val actionType = json.optString("action")
                    _proposedAction.value = ProposedAction(
                        type = actionType,
                        title = json.optString("title"),
                        description = json.optString("description"),
                        priority = json.optString("priority", "MEDIUM"),
                        category = json.optString("category", "عام"),
                        content = json.optString("content"),
                        offsetDays = json.optInt("dueDateOffsetDays", 0),
                        offsetHours = json.optInt("startTimeOffsetHours", 0),
                        frequency = json.optString("frequency", "DAILY")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Save Model message
            repository.insertMessage(
                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = cleanText,
                    detectedIntent = _proposedAction.value?.type
                )
            )

            // Auto Extract AI Memory Fact if the message was long and contained preferences
            extractMemoryFact(transcribedText, cleanText)
        }
    }

    fun executeProposedAction() {
        val action = _proposedAction.value ?: return
        viewModelScope.launch {
            when (action.type) {
                "CREATE_TASK" -> {
                    val cal = Calendar.getInstance()
                    if (action.offsetDays > 0) {
                        cal.add(Calendar.DAY_OF_YEAR, action.offsetDays)
                    }
                    repository.insertTask(
                        Task(
                            title = action.title ?: "مهمة ذكية جديدة",
                            description = action.description ?: "",
                            priority = action.priority ?: "MEDIUM",
                            dueDate = cal.timeInMillis
                        )
                    )
                }
                "CREATE_NOTE" -> {
                    repository.insertNote(
                        Note(
                            title = action.title ?: "ملاحظة ذكية جديدة",
                            content = action.content ?: "",
                            category = action.category ?: "عام"
                        )
                    )
                }
                "CREATE_EVENT" -> {
                    val cal = Calendar.getInstance()
                    if (action.offsetHours > 0) {
                        cal.add(Calendar.HOUR_OF_DAY, action.offsetHours)
                    }
                    val start = cal.timeInMillis
                    cal.add(Calendar.HOUR_OF_DAY, 1)
                    val end = cal.timeInMillis

                    repository.insertEvent(
                        CalendarEvent(
                            title = action.title ?: "حدث ذكي جديد",
                            description = action.description ?: "",
                            startTime = start,
                            endTime = end
                        )
                    )
                }
                "CREATE_HABIT" -> {
                    repository.insertHabit(
                        Habit(
                            title = action.title ?: "عادة جديدة",
                            frequency = action.frequency ?: "DAILY"
                        )
                    )
                }
            }
            _proposedAction.value = null
        }
    }

    fun dismissProposedAction() {
        _proposedAction.value = null
    }

    // --- AI Memory Extraction ---
    private fun extractMemoryFact(userMsg: String, modelReply: String) {
        viewModelScope.launch {
            // If message mentions preferences like "أنا أحب الكابتشينو" or "أنا أدرس البرمجة" or "أفضل القراءة بالليل"
            val prompt = """
                من هذه المحادثة القصيرة بين المستخدم والذكاء الاصطناعي، هل توجد أي حقائق أو تفضيلات دائمة وهامة عن المستخدم يرغب في أن يتذكرها مساعد الذكاء الاصطناعي في الجلسات القادمة؟ (مثل: تفضيلات القهوة، أوقات العمل المفضلة، أسماء مشاريع يعمل عليها، اهتمامات، هوايات، عادات).
                
                المحادثة:
                المستخدم: $userMsg
                المساعد: $modelReply
                
                إذا وجدت حقيقة هامة، أرجع كائن JSON واحد بالصيغة التالية، وبدون أي نص آخر إطلاقاً:
                {"found": true, "key": "اسم الحقيقة باختصار", "value": "التفصيل الكامل للحقيقة", "category": "person/project/habit/preference"}
                وإذا لم تجد أي حقيقة مفيدة، أرجع:
                {"found": false}
            """.trimIndent()

            val jsonResponse = RetrofitClient.callGemini(prompt = prompt, temperature = 0.2f)
            try {
                val cleanJson = jsonResponse.trim().substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                if (json.optBoolean("found", false)) {
                    val key = json.getString("key")
                    val value = json.getString("value")
                    val cat = json.optString("category", "preference")

                    // Insert to database if unique
                    repository.insertMemory(
                        AIMemory(
                            category = cat,
                            key = key,
                            value = value
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
    }

    fun deleteMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    // --- Voice to Task ---
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration = _recordingDuration.asStateFlow()

    private val _voiceProcessingState = MutableStateFlow<VoiceProcessingState>(VoiceProcessingState.Idle)
    val voiceProcessingState = _voiceProcessingState.asStateFlow()

    private var recordingJob: Job? = null

    fun startVoiceRecording() {
        _isRecording.value = true
        _recordingDuration.value = 0
        _voiceProcessingState.value = VoiceProcessingState.Recording
        recordingJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDuration.value += 1
                if (_recordingDuration.value >= 120) { // Max 2 min
                    stopVoiceRecordingAndProcess(null)
                    break
                }
            }
        }
    }

    fun stopVoiceRecordingAndProcess(customCommand: String?) {
        _isRecording.value = false
        recordingJob?.cancel()

        viewModelScope.launch {
            _voiceProcessingState.value = VoiceProcessingState.Transcribing
            delay(1500) // Simulate STT network delay

            // If user typed a custom mock command, we use it, otherwise we pick a smart mock command
            val transcribedText = customCommand ?: "سجل مهمة غسيل الملابس غدا الساعة 5 مساء بدرجة أهمية عالية"

            _voiceProcessingState.value = VoiceProcessingState.Extracting(transcribedText)

            val systemInstruction = """
                أنت خبير في استخراج البيانات من النصوص الصوتية المحولة إلى نصوص مقروءة.
                قم بتحليل النص المدخل واستخراج عناصر المهمة المطلوبة بدقة:
                - العنوان (title)
                - الوصف (description)
                - الأولوية (priority): يجب أن تكون إما "LOW" أو "MEDIUM" أو "HIGH" أو "URGENT"
                - عدد أيام الإزاحة عن اليوم (dueDateOffsetDays): رقم يمثل فارق الأيام. (اليوم 0، غداً 1، وهكذا)
                
                أرجع النتيجة في كائن JSON نقي ومباشر بالصيغة التالية تماماً وبدون أي كلام زائد:
                {"title": "عنوان المهمة", "description": "وصف المهمة", "priority": "الأولوية", "dueDateOffsetDays": N}
            """.trimIndent()

            val aiResponse = RetrofitClient.callGemini(
                prompt = transcribedText,
                systemInstruction = systemInstruction,
                temperature = 0.2f
            )

            try {
                val cleanJson = aiResponse.trim().substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                val title = json.getString("title")
                val desc = json.optString("description", "")
                val priority = json.optString("priority", "MEDIUM")
                val offset = json.optInt("dueDateOffsetDays", 0)

                _voiceProcessingState.value = VoiceProcessingState.Success(
                    Task(
                        title = title,
                        description = desc,
                        priority = priority,
                        dueDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }.timeInMillis
                    )
                )
            } catch (e: Exception) {
                _voiceProcessingState.value = VoiceProcessingState.Error("فشل في معالجة الصوت: ${e.localizedMessage}")
            }
        }
    }

    fun confirmVoiceTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
            _voiceProcessingState.value = VoiceProcessingState.Idle
        }
    }

    fun cancelVoiceTask() {
        _voiceProcessingState.value = VoiceProcessingState.Idle
    }

    // --- Task CRUD ---
    fun addTask(title: String, description: String, priority: String, dueDate: Long?, projectId: Int?) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    projectId = projectId
                )
            )
            // Re-calculate project progress if tied to project
            projectId?.let { updateProjectProgress(it) }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch {
            val updated = task.copy(status = newStatus)
            repository.updateTask(updated)
            task.projectId?.let { updateProjectProgress(it) }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            task.projectId?.let { updateProjectProgress(it) }
        }
    }

    // --- Project CRUD ---
    fun addProject(title: String, description: String, startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            repository.insertProject(
                Project(
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate
                )
            )
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    private suspend fun updateProjectProgress(projectId: Int) {
        val projectTasks = db.taskDao().getTasksByProject(projectId).first()
        if (projectTasks.isEmpty()) return
        val completed = projectTasks.count { it.status == "DONE" }
        val progress = (completed.toFloat() / projectTasks.size * 100).toInt()

        val project = repository.getProjectById(projectId)
        if (project != null) {
            repository.updateProject(project.copy(progressPercent = progress))
        }
    }

    // --- Smart Notes CRUD ---
    fun addNoteWithAICategorization(title: String, content: String) {
        viewModelScope.launch {
            _isChatLoading.value = true // Show loading indicator
            val prompt = """
                قم بتحليل محتوى الملاحظة التالية واقترح تصنيفاً واحداً قصيراً جداً (مثل: دراسة، عمل، صحة، فكرة، شخصي، رياضة)، ومجموعة من الكلمات الدلالية المناسبة (tags).
                
                الملاحظة:
                العنوان: $title
                المحتوى: $content
                
                أرجع الرد في تنسيق JSON نقي بالصيغة التالية وبدون أي نص آخر:
                {"category": "التصنيف المقترح", "tags": ["علامة1", "علامة2"]}
            """.trimIndent()

            val aiResponse = RetrofitClient.callGemini(prompt = prompt, temperature = 0.3f)
            var category = "عام"
            var tagsJson = "[]"

            try {
                val cleanJson = aiResponse.trim().substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                category = json.getString("category")
                val tagsArray = json.getJSONArray("tags")
                tagsJson = tagsArray.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            repository.insertNote(
                Note(
                    title = title,
                    content = content,
                    category = category,
                    tagsJson = tagsJson
                )
            )
            _isChatLoading.value = false
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Calendar CRUD ---
    fun addCalendarEvent(title: String, description: String, startTime: Long, endTime: Long, location: String) {
        viewModelScope.launch {
            repository.insertEvent(
                CalendarEvent(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    location = location
                )
            )
        }
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // --- Goals & Habits ---
    fun addGoal(title: String, type: String, targetValue: Int) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, type = type, targetValue = targetValue))
        }
    }

    fun updateGoalProgress(goal: Goal, delta: Int) {
        viewModelScope.launch {
            val newVal = (goal.currentValue + delta).coerceIn(0, goal.targetValue)
            repository.updateGoal(goal.copy(currentValue = newVal))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addHabit(title: String, frequency: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(title = title, frequency = frequency))
        }
    }

    fun checkInHabit(habit: Habit) {
        viewModelScope.launch {
            val logs = habit.logsJson
            val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val logsList = mutableListOf<String>()
            try {
                val arr = org.json.JSONArray(logs)
                for (i in 0 until arr.length()) {
                    logsList.add(arr.getString(i))
                }
            } catch (e: Exception) { }

            if (!logsList.contains(nowStr)) {
                logsList.add(nowStr)
                val newStreak = habit.streakCount + 1
                repository.updateHabit(
                    habit.copy(
                        streakCount = newStreak,
                        logsJson = org.json.JSONArray(logsList).toString()
                    )
                )
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // --- Pomodoro Timer State ---
    private val _pomodoroSecondsLeft = MutableStateFlow(1500) // 25 min default
    val pomodoroSecondsLeft = _pomodoroSecondsLeft.asStateFlow()

    private val _pomodoroIsActive = MutableStateFlow(false)
    val pomodoroIsActive = _pomodoroIsActive.asStateFlow()

    private val _pomodoroMode = MutableStateFlow("WORK") // WORK, BREAK
    val pomodoroMode = _pomodoroMode.asStateFlow()

    private var pomodoroJob: Job? = null

    fun startPomodoro() {
        if (_pomodoroIsActive.value) return
        _pomodoroIsActive.value = true
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroSecondsLeft.value > 0 && _pomodoroIsActive.value) {
                delay(1000)
                _pomodoroSecondsLeft.value -= 1
            }
            if (_pomodoroSecondsLeft.value == 0) {
                // Timer finished!
                if (_pomodoroMode.value == "WORK") {
                    _pomodoroMode.value = "BREAK"
                    _pomodoroSecondsLeft.value = 300 // 5 min break
                } else {
                    _pomodoroMode.value = "WORK"
                    _pomodoroSecondsLeft.value = 1500 // 25 min work
                }
                _pomodoroIsActive.value = false
            }
        }
    }

    fun pausePomodoro() {
        _pomodoroIsActive.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        _pomodoroMode.value = "WORK"
        _pomodoroSecondsLeft.value = 1500
    }

    // --- Documents AI & OCR Mock Scans ---
    private val _isDocLoading = MutableStateFlow(false)
    val isDocLoading = _isDocLoading.asStateFlow()

    private val _ocrText = MutableStateFlow("")
    val ocrText = _ocrText.asStateFlow()

    private val _docSummary = MutableStateFlow("")
    val docSummary = _docSummary.asStateFlow()

    private val _docTasks = MutableStateFlow<List<String>>(emptyList())
    val docTasks = _docTasks.asStateFlow()

    fun processDocument(title: String, textContent: String, type: String) {
        viewModelScope.launch {
            _isDocLoading.value = true
            _docSummary.value = ""
            _docTasks.value = emptyList()

            val prompt = """
                قم بتحليل المستند التالي بعنوان "$title":
                $textContent
                
                المطلوب:
                1. تلخيص مركز للمستند باللغة العربية (لا يتجاوز 150 كلمة).
                2. استخراج أهم 3-5 نقاط رئيسية (Key points).
                3. استخراج أي مهام أو إجراءات مقترحة أو مطلوبة واردة في النص (Suggested tasks).
                
                أرجع النتيجة بتنسيق JSON نقي ومباشر بالصيغة التالية وبدون أي أحرف زائدة:
                {"summary": "ملخص المستند هنا...", "keyPoints": ["نقطة 1", "نقطة 2"], "tasks": ["مهمة 1", "مهمة 2"]}
            """.trimIndent()

            val response = RetrofitClient.callGemini(prompt = prompt, temperature = 0.4f)
            try {
                val cleanJson = response.trim().substringAfter("{").substringBeforeLast("}")
                val json = JSONObject("{$cleanJson}")
                val summary = json.getString("summary")
                val kpArray = json.getJSONArray("keyPoints")
                val tasksArray = json.getJSONArray("tasks")

                val kpList = mutableListOf<String>()
                for (i in 0 until kpArray.length()) { kpList.add(kpArray.getString(i)) }

                val tasksList = mutableListOf<String>()
                for (i in 0 until tasksArray.length()) { tasksList.add(tasksArray.getString(i)) }

                _docSummary.value = summary
                _docTasks.value = tasksList

                // Insert into Database
                repository.insertDocument(
                    Document(
                        title = title,
                        content = textContent,
                        summary = summary,
                        keyPointsJson = kpArray.toString(),
                        suggestedTasksJson = tasksArray.toString(),
                        type = type
                    )
                )

                // Also insert extracted tasks into actual Task manager
                tasksList.forEach { taskTitle ->
                    repository.insertTask(
                        Task(
                            title = taskTitle,
                            description = "مهمة مستخرجة تلقائياً من مستند: $title",
                            priority = "MEDIUM"
                        )
                    )
                }

            } catch (e: Exception) {
                _docSummary.value = "فشل تحليل المستند: ${e.localizedMessage}"
            } finally {
                _isDocLoading.value = false
            }
        }
    }

    // --- Email Assistant ---
    private val _emailDraft = MutableStateFlow("")
    val emailDraft = _emailDraft.asStateFlow()

    private val _emailLoading = MutableStateFlow(false)
    val emailLoading = _emailLoading.asStateFlow()

    fun draftEmail(context: String, tone: String) {
        viewModelScope.launch {
            _emailLoading.value = true
            val prompt = """
                اكتب بريداً إلكترونياً احترافياً باللغة العربية بناءً على السياق التالي:
                السياق: $context
                الأسلوب والنبرة المطلوبة: $tone
                
                تأكد من صياغة عنوان مناسب ومحتوى بريد منظم ومكتمل التحية والختام بشكل رائع.
            """.trimIndent()

            val response = RetrofitClient.callGemini(prompt = prompt, temperature = 0.7f)
            _emailDraft.value = response
            _emailLoading.value = false
        }
    }

    fun summarizeEmail(rawEmail: String) {
        viewModelScope.launch {
            _emailLoading.value = true
            val prompt = """
                قم بتلخيص البريد الإلكتروني التالي واقترح رداً مناسباً له:
                $rawEmail
                
                أرجع الملخص متبوعاً بمقترحين للرد باللغة العربية بشكل أنيق وسهل القراءة.
            """.trimIndent()

            val response = RetrofitClient.callGemini(prompt = prompt, temperature = 0.5f)
            _emailDraft.value = response
            _emailLoading.value = false
        }
    }
}

// Helper models
data class ProposedAction(
    val type: String,
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val category: String? = null,
    val content: String? = null,
    val offsetDays: Int = 0,
    val offsetHours: Int = 0,
    val frequency: String? = null
)

sealed interface VoiceProcessingState {
    object Idle : VoiceProcessingState
    object Recording : VoiceProcessingState
    object Transcribing : VoiceProcessingState
    data class Extracting(val text: String) : VoiceProcessingState
    data class Success(val extractedTask: Task) : VoiceProcessingState
    data class Error(val error: String) : VoiceProcessingState
}
