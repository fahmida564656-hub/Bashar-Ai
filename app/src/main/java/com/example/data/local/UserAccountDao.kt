package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getAccountByPhone(phone: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInAccountFlow(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInAccount(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Update
    suspend fun updateAccount(account: UserAccountEntity)

    @Query("UPDATE user_accounts SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE user_accounts SET isLoggedIn = 1 WHERE phoneNumber = :phone")
    suspend fun setLoggedIn(phone: String)

    @Query("UPDATE user_accounts SET passwordHash = :newPasswordHash WHERE phoneNumber = :phone")
    suspend fun updatePassword(phone: String, newPasswordHash: String)
}
