package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val mode: String = "QUICK",
    val projectId: String? = null,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val hasSources: Boolean = false,
    val sourcesJson: String? = null,
    val isLiked: Boolean? = null
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val systemPrompt: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val memoryText: String,
    val category: String = "GENERAL",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "canvas_docs")
data class CanvasDocEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
