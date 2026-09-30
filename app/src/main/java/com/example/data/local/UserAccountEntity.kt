package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey
    val phoneNumber: String,
    val fullName: String,
    val passwordHash: String,
    val recoverySecretName: String, // Secret name to recover forgotten password
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false
)
