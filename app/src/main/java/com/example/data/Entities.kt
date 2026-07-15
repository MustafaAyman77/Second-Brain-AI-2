package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val status: String = "TODO",     // TODO, IN_PROGRESS, DONE
    val dueDate: Long? = null,
    val projectId: Int? = null,
    val subtasksJson: String = "[]",  // JSON list of subtasks: [{"title":"...", "isDone":false}]
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val progressPercent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val tagsJson: String = "[]",          // JSON list of strings
    val linkedNotesJson: String = "[]",   // JSON list of linked Note IDs
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String = "DAILY",           // DAILY, WEEKLY, MONTHLY, YEARLY
    val targetValue: Int = 100,
    val currentValue: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val frequency: String = "DAILY",      // DAILY, WEEKLY
    val streakCount: Int = 0,
    val logsJson: String = "[]",          // JSON list of timestamps (dates checked in)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val location: String = "",
    val syncedWithGoogle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_memories")
data class AIMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,                 // person, project, habit, preference, fact
    val key: String,
    val value: String,
    val sourceChatId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val role: String,                     // user, model
    val content: String,
    val detectedIntent: String? = null,   // TASK_CREATE, NOTE_CREATE, REMINDER_CREATE, etc.
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val summary: String = "",
    val keyPointsJson: String = "[]",     // JSON list of key points
    val suggestedTasksJson: String = "[]", // JSON list of tasks
    val type: String = "TEXT",            // TEXT, PDF, IMAGE
    val createdAt: Long = System.currentTimeMillis()
)

data class Subtask(
    val title: String,
    val isDone: Boolean = false
)
