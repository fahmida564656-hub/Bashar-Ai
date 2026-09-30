package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey
    val id: String,
    val userPhone: String,
    val fileName: String,
    val fileSize: Long, // Size in bytes
    val mimeType: String,
    val category: String, // DOCUMENT, PHOTO, AUDIO, VIDEO, ARCHIVE, OTHER
    val localFilePath: String,
    val uploadedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
