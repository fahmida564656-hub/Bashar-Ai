package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultFileDao {
    @Query("SELECT * FROM vault_files WHERE userPhone = :phone ORDER BY uploadedAt DESC")
    fun getFilesByUser(phone: String): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files ORDER BY uploadedAt DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT SUM(fileSize) FROM vault_files WHERE userPhone = :phone")
    fun getTotalStorageUsed(phone: String): Flow<Long?>

    @Query("SELECT SUM(fileSize) FROM vault_files")
    fun getOverallStorageUsed(): Flow<Long?>

    @Query("SELECT * FROM vault_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): VaultFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Update
    suspend fun updateFile(file: VaultFileEntity)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFileById(id: String)

    @Query("DELETE FROM vault_files WHERE userPhone = :phone")
    suspend fun deleteAllFilesForUser(phone: String)
}
