package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AppDatabase
import com.example.data.local.CanvasDocEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.MessageEntity
import com.example.data.local.ProjectEntity
import com.example.data.model.ChatMode
import com.example.data.model.ResearchStep
import com.example.data.model.SourceCitation
import com.example.data.model.UserPreferences
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import com.example.data.local.UserAccountDao
import com.example.data.local.UserAccountEntity
import com.example.data.local.VaultFileDao
import com.example.data.local.VaultFileEntity
import java.io.File
import java.io.InputStream
import java.util.UUID

class BasharAiRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val projectDao = db.projectDao()
    private val memoryDao = db.memoryDao()
    private val canvasDao = db.canvasDao()
    private val userAccountDao = db.userAccountDao()
    private val vaultFileDao = db.vaultFileDao()

    private val geminiClient = GeminiApiClient()

    // Conversations
    fun getAllConversations(): Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
    fun getConversationsByProject(projectId: String): Flow<List<ConversationEntity>> =
        conversationDao.getConversationsByProject(projectId)

    suspend fun createConversation(title: String, mode: ChatMode = ChatMode.QUICK, projectId: String? = null): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(
            id = id,
            title = title,
            mode = mode.name,
            projectId = projectId
        )
        conversationDao.insertConversation(conv)
        return id
    }

    suspend fun updateConversationTitle(id: String, newTitle: String) {
        val conv = conversationDao.getConversationById(id) ?: return
        conversationDao.updateConversation(conv.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteConversation(id: String) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearAllChatHistory() {
        messageDao.deleteAllMessages()
        conversationDao.deleteAllConversations()
    }

    // Messages
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(convId)

    suspend fun getMessagesList(convId: String): List<MessageEntity> =
        messageDao.getMessagesList(convId)

    suspend fun saveUserMessage(
        conversationId: String,
        content: String,
        imageUri: String? = null
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = "user",
            content = content,
            imageUri = imageUri
        )
        messageDao.insertMessage(message)

        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            val updatedTitle = if (conv.title == "নতুন কথোপকথন" || conv.title == "New Chat") {
                content.take(30).trim()
            } else {
                conv.title
            }
            conversationDao.updateConversation(
                conv.copy(title = updatedTitle, updatedAt = System.currentTimeMillis())
            )
        }
        return message
    }

    suspend fun saveModelMessage(
        conversationId: String,
        content: String,
        sources: List<SourceCitation> = emptyList()
    ): MessageEntity {
        val sourcesJson = if (sources.isNotEmpty()) {
            val array = JSONArray()
            for (s in sources) {
                val obj = JSONObject().apply {
                    put("title", s.title)
                    put("domain", s.domain)
                    put("snippet", s.snippet)
                    put("url", s.url)
                }
                array.put(obj)
            }
            array.toString()
        } else null

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = "model",
            content = content,
            hasSources = sources.isNotEmpty(),
            sourcesJson = sourcesJson
        )
        messageDao.insertMessage(message)

        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
        return message
    }

    suspend fun updateMessageFeedback(messageId: String, isLiked: Boolean?) {
        messageDao.updateMessageFeedback(messageId, isLiked)
    }

    // AI Generation
    suspend fun askGemini(
        conversationId: String,
        prompt: String,
        mode: ChatMode,
        imageBitmap: Bitmap? = null,
        preferences: UserPreferences,
        onResearchStep: (suspend (List<ResearchStep>) -> Unit)? = null
    ): GeminiResult {
        // Collect past 6 turns for conversation memory
        val pastEntities = messageDao.getMessagesList(conversationId).takeLast(6)
        val history = pastEntities.map { it.role to it.content }

        // Inject explicit User Memories if enabled
        val memoryContext = if (preferences.memoryEnabled) {
            val memories = memoryDao.getMemoriesList()
            if (memories.isNotEmpty()) {
                "ব্যবহারকারীর স্মরণীয় পছন্দসমূহ (User Memories):\n" +
                        memories.joinToString("\n") { "• ${it.memoryText}" }
            } else null
        } else null

        // If Deep Research mode, dispatch step milestones
        if (mode == ChatMode.RESEARCH && onResearchStep != null) {
            val steps = mutableListOf(
                ResearchStep(1, "প্রশ্ন বিশ্লেষণ", "মূল বিষয়বস্তু ও প্রেক্ষিত চিহ্নিত করা হচ্ছে...", isActive = true),
                ResearchStep(2, "উপ-প্রশ্নে বিভাজন", "বহুমাত্রিক দিকসমূহের পরিকল্পনা প্রস্তুত করা হচ্ছে..."),
                ResearchStep(3, "তথ্য সংকলন ও যাচাই", "উপাত্ত ও নলেজবেস স্ক্যান করা হচ্ছে..."),
                ResearchStep(4, "তুলনা ও গভীর সংশ্লেষণ", "তথ্যের সত্যতা ও প্রাসঙ্গিকতা যাচাইকরণ..."),
                ResearchStep(5, "প্রতিবেদন প্রস্তুতকরণ", "পূর্ণাঙ্গ গবেষণা প্রতিবেদন প্রণয়ন...")
            )
            onResearchStep(steps.toList())
            delay(500)

            steps[0] = steps[0].copy(isDone = true, isActive = false)
            steps[1] = steps[1].copy(isActive = true)
            onResearchStep(steps.toList())
            delay(500)

            steps[1] = steps[1].copy(isDone = true, isActive = false)
            steps[2] = steps[2].copy(isActive = true)
            onResearchStep(steps.toList())
            delay(500)

            steps[2] = steps[2].copy(isDone = true, isActive = false)
            steps[3] = steps[3].copy(isActive = true)
            onResearchStep(steps.toList())
            delay(500)

            steps[3] = steps[3].copy(isDone = true, isActive = false)
            steps[4] = steps[4].copy(isActive = true)
            onResearchStep(steps.toList())
        }

        val result = geminiClient.generateContent(
            prompt = prompt,
            mode = mode,
            history = history,
            imageBitmap = imageBitmap,
            apiKeyOverride = preferences.customApiKey,
            modelOverride = preferences.selectedModel,
            customSystemPrompt = memoryContext,
            webSearchEnabled = preferences.webSearchEnabled
        )

        return result
    }

    // Projects
    fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    suspend fun createProject(title: String, description: String, category: String, instructions: String): String {
        val id = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            systemPrompt = instructions
        )
        projectDao.insertProject(project)
        return id
    }
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    // Memories
    fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    suspend fun addMemory(text: String, category: String = "GENERAL") {
        val id = UUID.randomUUID().toString()
        memoryDao.insertMemory(MemoryEntity(id = id, memoryText = text, category = category))
    }
    suspend fun deleteMemory(id: String) = memoryDao.deleteMemoryById(id)
    suspend fun clearAllMemories() = memoryDao.clearAllMemories()

    // Canvas
    fun getAllDocs(): Flow<List<CanvasDocEntity>> = canvasDao.getAllDocs()
    suspend fun getDocById(id: String) = canvasDao.getDocById(id)
    suspend fun saveDoc(id: String?, title: String, content: String): String {
        val docId = id ?: UUID.randomUUID().toString()
        canvasDao.insertDoc(
            CanvasDocEntity(
                id = docId,
                title = title.ifBlank { "শিরোনামহীন খসড়া" },
                content = content,
                updatedAt = System.currentTimeMillis()
            )
        )
        return docId
    }
    suspend fun deleteDoc(id: String) = canvasDao.deleteDocById(id)

    // User Authentication & Account Management
    fun getLoggedInAccountFlow(): Flow<UserAccountEntity?> = userAccountDao.getLoggedInAccountFlow()
    suspend fun getLoggedInAccount(): UserAccountEntity? = userAccountDao.getLoggedInAccount()

    suspend fun registerAccount(
        phone: String,
        fullName: String,
        password: String,
        recoverySecretName: String
    ): Result<UserAccountEntity> {
        val cleanPhone = phone.trim().filter { it.isDigit() || it == '+' }
        if (cleanPhone.length < 6) {
            return Result.failure(IllegalArgumentException("সঠিক মোবাইল নম্বর প্রদান করুন"))
        }
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("আপনার পূর্ণ নাম প্রদান করুন"))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"))
        }
        if (recoverySecretName.isBlank()) {
            return Result.failure(IllegalArgumentException("পাসওয়ার্ড রিকভারির জন্য একটি নাম সেট করুন"))
        }

        val existing = userAccountDao.getAccountByPhone(cleanPhone)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("এই নম্বরে ইতিমধ্যে একটি একাউন্ট রয়েছে। অনুগ্রহ করে লগইন করুন।"))
        }

        userAccountDao.logoutAll()

        val newAccount = UserAccountEntity(
            phoneNumber = cleanPhone,
            fullName = fullName.trim(),
            passwordHash = hashPassword(password),
            recoverySecretName = recoverySecretName.trim(),
            createdAt = System.currentTimeMillis(),
            isLoggedIn = true
        )
        userAccountDao.insertAccount(newAccount)
        return Result.success(newAccount)
    }

    suspend fun loginAccount(phone: String, password: String): Result<UserAccountEntity> {
        val cleanPhone = phone.trim().filter { it.isDigit() || it == '+' }
        val account = userAccountDao.getAccountByPhone(cleanPhone)
            ?: return Result.failure(IllegalArgumentException("এই নম্বরে কোনো একাউন্ট পাওয়া যায়নি। সাইন আপ করুন।"))

        if (account.passwordHash != hashPassword(password)) {
            return Result.failure(IllegalArgumentException("ভুল পাসওয়ার্ড। পাসওয়ার্ড ভুলে গেলে রিকভারি অপশন ব্যবহার করুন।"))
        }

        userAccountDao.logoutAll()
        userAccountDao.setLoggedIn(cleanPhone)
        return Result.success(account.copy(isLoggedIn = true))
    }

    suspend fun recoverPasswordWithSecretName(
        phone: String,
        secretName: String,
        newPassword: String
    ): Result<Boolean> {
        val cleanPhone = phone.trim().filter { it.isDigit() || it == '+' }
        val account = userAccountDao.getAccountByPhone(cleanPhone)
            ?: return Result.failure(IllegalArgumentException("এই নম্বরে কোনো একাউন্ট নিবন্ধিত নেই"))

        val expectedSecret = account.recoverySecretName.trim().lowercase()
        val providedSecret = secretName.trim().lowercase()

        if (expectedSecret != providedSecret) {
            return Result.failure(IllegalArgumentException("সিক্রেট নামটি মেলেনি। সঠিক রিকভারি নাম দিন।"))
        }

        if (newPassword.length < 4) {
            return Result.failure(IllegalArgumentException("নতুন পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"))
        }

        userAccountDao.updatePassword(cleanPhone, hashPassword(newPassword))
        return Result.success(true)
    }

    suspend fun logout() {
        userAccountDao.logoutAll()
    }

    private fun hashPassword(password: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // 100GB Cloud / Local File Vault
    fun getVaultFiles(userPhone: String?): Flow<List<VaultFileEntity>> {
        return if (!userPhone.isNullOrBlank()) {
            vaultFileDao.getFilesByUser(userPhone)
        } else {
            vaultFileDao.getAllFiles()
        }
    }

    fun getTotalStorageUsed(userPhone: String?): Flow<Long?> {
        return if (!userPhone.isNullOrBlank()) {
            vaultFileDao.getTotalStorageUsed(userPhone)
        } else {
            vaultFileDao.getOverallStorageUsed()
        }
    }

    suspend fun saveFileToVault(
        userPhone: String,
        fileName: String,
        mimeType: String,
        inputStream: InputStream
    ): Result<VaultFileEntity> {
        return try {
            val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
            val fileId = UUID.randomUUID().toString()
            val safeFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(vaultDir, "${fileId}_$safeFileName")

            var totalBytes = 0L
            targetFile.outputStream().use { out ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
            }

            val category = detectFileCategory(fileName, mimeType)

            val vaultEntity = VaultFileEntity(
                id = fileId,
                userPhone = userPhone,
                fileName = fileName,
                fileSize = totalBytes,
                mimeType = mimeType,
                category = category,
                localFilePath = targetFile.absolutePath,
                uploadedAt = System.currentTimeMillis()
            )
            vaultFileDao.insertFile(vaultEntity)
            Result.success(vaultEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVaultFile(id: String) {
        val fileEntity = vaultFileDao.getFileById(id)
        if (fileEntity != null) {
            try {
                val f = File(fileEntity.localFilePath)
                if (f.exists()) f.delete()
            } catch (e: Exception) {
                // ignore
            }
            vaultFileDao.deleteFileById(id)
        }
    }

    suspend fun toggleFavoriteFile(id: String) {
        val fileEntity = vaultFileDao.getFileById(id) ?: return
        vaultFileDao.updateFile(fileEntity.copy(isFavorite = !fileEntity.isFavorite))
    }

    private fun detectFileCategory(fileName: String, mimeType: String): String {
        val lowerName = fileName.lowercase()
        val lowerMime = mimeType.lowercase()
        return when {
            lowerMime.startsWith("image/") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                    lowerName.endsWith(".png") || lowerName.endsWith(".webp") || lowerName.endsWith(".gif") -> "PHOTO"

            lowerMime.startsWith("audio/") || lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") ||
                    lowerName.endsWith(".m4a") || lowerName.endsWith(".aac") -> "AUDIO"

            lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") ||
                    lowerName.endsWith(".avi") -> "VIDEO"

            lowerMime.contains("pdf") || lowerMime.contains("word") || lowerMime.contains("text") ||
                    lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx") ||
                    lowerName.endsWith(".txt") -> "DOCUMENT"

            lowerName.endsWith(".zip") || lowerName.endsWith(".rar") || lowerName.endsWith(".tar") ||
                    lowerName.endsWith(".gz") || lowerName.endsWith(".7z") -> "ARCHIVE"

            else -> "OTHER"
        }
    }
}
