package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val imageUrl: String? = null,
    val plan: String = "FREE" // FREE, PRO
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val status: String = "TODO", // TODO, DOING, DONE
    val priority: String = "MEDIUM", // URGENT, HIGH, MEDIUM, LOW
    val dueDate: Long? = null,
    val projectId: Int? = null,
    val subtasksJson: String = "[]", // serialized JSON array
    val progress: Float = 0f
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val progress: Float = 0f,
    val dueDate: Long? = null
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ideas")
data class Idea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "General",
    val aiSuggestions: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val period: String = "DAILY", // DAILY, WEEKLY, MONTHLY, YEARLY
    val progress: Float = 0f,
    val target: Float = 100f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val frequency: String = "DAILY",
    val streak: Int = 0,
    val complianceRate: Float = 0f,
    val historyStr: String = "", // Comma-separated dates e.g. "2026-07-15,2026-07-16"
    val lastDone: Long? = null
)

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mimeType: String,
    val summary: String = "",
    val extractedTasks: String = "",
    val path: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val eventTime: Long,
    val location: String = "",
    val syncStatus: Boolean = false
)

@Entity(tableName = "chats")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // USER, ASSISTANT
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_memory")
data class AiMemory(
    @PrimaryKey val key: String,
    val value: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val theme: String = "DARK", // DARK, LIGHT
    val language: String = "AR", // AR, EN
    val backupEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)
