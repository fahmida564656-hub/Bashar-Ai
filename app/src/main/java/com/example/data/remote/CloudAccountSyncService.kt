package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.data.local.UserAccountEntity
import com.example.data.local.VaultFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class CloudSyncData(
    val phoneNumber: String,
    val fullName: String,
    val passwordHash: String,
    val secretRecoveryName: String,
    val createdAt: Long,
    val vaultFiles: List<CloudVaultFileItem> = emptyList()
)

data class CloudVaultFileItem(
    val id: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val category: String,
    val uploadedAt: Long,
    val isFavorite: Boolean
)

object CloudAccountSyncService {
    private const val TAG = "CloudAccountSync"
    private const val APP_KEY = "qqd3ctvc"
    private const val BASE_URL = "https://keyvalue.immanuel.co/api/KeyVal"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun sanitizeKey(phoneNumber: String): String {
        return "user_" + phoneNumber.replace("[^0-9a-zA-Z]".toRegex(), "")
    }

    suspend fun pushAccountToCloud(
        account: UserAccountEntity,
        vaultFiles: List<VaultFileEntity> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = sanitizeKey(account.phoneNumber)
            val json = JSONObject().apply {
                put("phoneNumber", account.phoneNumber)
                put("fullName", account.fullName)
                put("passwordHash", account.passwordHash)
                put("secretRecoveryName", account.secretRecoveryName)
                put("createdAt", account.createdAt)

                val filesArray = JSONArray()
                vaultFiles.forEach { file ->
                    val fileObj = JSONObject().apply {
                        put("id", file.id)
                        put("fileName", file.fileName)
                        put("fileSize", file.fileSize)
                        put("mimeType", file.mimeType)
                        put("category", file.category)
                        put("uploadedAt", file.uploadedAt)
                        put("isFavorite", file.isFavorite)
                    }
                    filesArray.put(fileObj)
                }
                put("vaultFiles", filesArray)
            }

            val rawBytes = json.toString().toByteArray(StandardCharsets.UTF_8)
            val encodedValue = Base64.encodeToString(rawBytes, Base64.URL_SAFE or Base64.NO_WRAP)

            val url = "$BASE_URL/UpdateValue/$APP_KEY/$key/$encodedValue"
            val emptyBody = "".toRequestBody(null)
            val request = Request.Builder()
                .url(url)
                .post(emptyBody)
                .addHeader("Content-Length", "0")
                .build()

            val response = httpClient.newCall(request).execute()
            val respStr = response.body?.string()?.trim() ?: ""
            respStr.contains("true")
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing account to cloud: ${e.message}")
            false
        }
    }

    suspend fun pullAccountFromCloud(phoneNumber: String): CloudSyncData? = withContext(Dispatchers.IO) {
        try {
            val key = sanitizeKey(phoneNumber)
            val url = "$BASE_URL/GetValue/$APP_KEY/$key"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            var raw = response.body?.string()?.trim() ?: return@withContext null
            if (raw == "null" || raw.isEmpty() || raw == "\"\"") return@withContext null

            // Strip surrounding quotes if JSON serialized string
            if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length >= 2) {
                raw = raw.substring(1, raw.length - 1)
            }

            val decodedBytes = Base64.decode(raw, Base64.URL_SAFE or Base64.DEFAULT)
            val jsonStr = String(decodedBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonStr)

            val filesList = mutableListOf<CloudVaultFileItem>()
            val filesArray = json.optJSONArray("vaultFiles")
            if (filesArray != null) {
                for (i in 0 until filesArray.length()) {
                    val obj = filesArray.getJSONObject(i)
                    filesList.add(
                        CloudVaultFileItem(
                            id = obj.getString("id"),
                            fileName = obj.getString("fileName"),
                            fileSize = obj.getLong("fileSize"),
                            mimeType = obj.getString("mimeType"),
                            category = obj.getString("category"),
                            uploadedAt = obj.getLong("uploadedAt"),
                            isFavorite = obj.optBoolean("isFavorite", false)
                        )
                    )
                }
            }

            CloudSyncData(
                phoneNumber = json.getString("phoneNumber"),
                fullName = json.getString("fullName"),
                passwordHash = json.getString("passwordHash"),
                secretRecoveryName = json.optString("secretRecoveryName", ""),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                vaultFiles = filesList
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error pulling account from cloud: ${e.message}")
            null
        }
    }

    suspend fun updatePasswordInCloud(
        phoneNumber: String,
        newPasswordHash: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val existing = pullAccountFromCloud(phoneNumber) ?: return@withContext false
            val updated = UserAccountEntity(
                phoneNumber = existing.phoneNumber,
                fullName = existing.fullName,
                passwordHash = newPasswordHash,
                secretRecoveryName = existing.secretRecoveryName,
                isLoggedIn = true,
                createdAt = existing.createdAt
            )
            val vaultEntities = existing.vaultFiles.map { f ->
                VaultFileEntity(
                    id = f.id,
                    userPhoneNumber = existing.phoneNumber,
                    fileName = f.fileName,
                    localFilePath = "",
                    fileSize = f.fileSize,
                    mimeType = f.mimeType,
                    category = f.category,
                    uploadedAt = f.uploadedAt,
                    isFavorite = f.isFavorite
                )
            }
            pushAccountToCloud(updated, vaultEntities)
        } catch (e: Exception) {
            false
        }
    }
}
