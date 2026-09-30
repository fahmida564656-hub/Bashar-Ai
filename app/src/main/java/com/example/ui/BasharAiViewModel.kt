package com.example.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.data.local.CanvasDocEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.VaultFileEntity
import com.example.data.model.ChatMode
import com.example.data.model.UserPreferences
import com.example.data.remote.GeminiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Chat : ScreenDestination()
    object ToolsHub : ScreenDestination()
    object Canvas : ScreenDestination()
    object CodeStudio : ScreenDestination()
    object Calculator : ScreenDestination()
    object Projects : ScreenDestination()
    object Memory : ScreenDestination()
    object Settings : ScreenDestination()
    object LiveVoice : ScreenDestination()
    object StorageVault : ScreenDestination()
    object Auth : ScreenDestination()
}

class BasharAiViewModel(application: Application) : ChatViewModel(application) {

    // Navigation
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Chat)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Conversations List
    val conversations: StateFlow<List<ConversationEntity>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Projects
    val projects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProject = MutableStateFlow<ProjectEntity?>(null)
    val selectedProject: StateFlow<ProjectEntity?> = _selectedProject.asStateFlow()

    // Memories
    val memories: StateFlow<List<MemoryEntity>> = repository.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Canvas
    val canvasDocs: StateFlow<List<CanvasDocEntity>> = repository.getAllDocs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeCanvasDoc = MutableStateFlow<CanvasDocEntity?>(null)
    val activeCanvasDoc: StateFlow<CanvasDocEntity?> = _activeCanvasDoc.asStateFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    override fun startNewChat(mode: ChatMode) {
        viewModelScope.launch {
            val newId = repository.createConversation(
                title = "নতুন কথোপকথন",
                mode = mode,
                projectId = _selectedProject.value?.id
            )
            _currentMode.value = mode
            _currentConversationId.value = newId
            _messages.value = emptyList()
            _suggestedFollowUps.value = emptyList()
            _researchSteps.value = emptyList()
            _currentScreen.value = ScreenDestination.Chat
            selectConversation(newId)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
                _messages.value = emptyList()
                startNewChat()
            }
        }
    }

    // Projects
    fun createProject(title: String, description: String, category: String, instructions: String) {
        viewModelScope.launch {
            repository.createProject(title, description, category, instructions)
        }
    }

    fun selectProject(project: ProjectEntity?) {
        _selectedProject.value = project
        if (project != null) {
            startNewChat()
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_selectedProject.value?.id == project.id) {
                _selectedProject.value = null
            }
        }
    }

    // Memories
    fun addMemory(text: String, category: String = "GENERAL") {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                repository.addMemory(text.trim(), category)
            }
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // Canvas
    fun loadCanvasDoc(doc: CanvasDocEntity?) {
        _activeCanvasDoc.value = doc
    }

    fun saveCanvasDoc(title: String, content: String) {
        viewModelScope.launch {
            val docId = repository.saveDoc(_activeCanvasDoc.value?.id, title, content)
            _activeCanvasDoc.value = repository.getDocById(docId)
        }
    }

    fun processCanvasAiAction(actionType: String, currentText: String, onResult: (String) -> Unit) {
        if (currentText.isBlank()) return
        viewModelScope.launch {
            _isGenerating.value = true
            val prompt = when (actionType) {
                "REWRITE" -> "নিচের লেখাটি অত্যন্ত মার্জিত, প্রফেশনাল ও আকর্ষণীয় ভাষায় পুনর্লিখন করুন:\n\n$currentText"
                "SHORTEN" -> "নিচের লেখাটি মূল ভাব বজায় রেখে সংক্ষেপে বুলেট পয়েন্ট আকারে লিখুন:\n\n$currentText"
                "EXPAND" -> "নিচের লেখাটিতে বিস্তারিত তথ্য, গভীর বিশ্লেষণ ও উদাহরণ সংযোগ করে বড় করুন:\n\n$currentText"
                "TRANSLATE_EN" -> "Translate the following text accurately into professional English:\n\n$currentText"
                "TRANSLATE_BN" -> "নিচের লেখাটি চমৎকার ও প্রাঞ্জল বাংলায় অনুবাদ করুন:\n\n$currentText"
                else -> currentText
            }
            val res = repository.askGemini(
                conversationId = _currentConversationId.value ?: "canvas_session",
                prompt = prompt,
                mode = ChatMode.WRITING,
                preferences = _preferences.value
            )
            _isGenerating.value = false
            if (res is GeminiResult.Success) {
                onResult(res.text)
            }
        }
    }

    // Settings Updates
    fun updatePreferences(newPrefs: UserPreferences) {
        _preferences.value = newPrefs
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllChatHistory()
            repository.clearAllMemories()
            _messages.value = emptyList()
            _currentConversationId.value = null
            startNewChat()
        }
    }

    // User Authentication & Account
    val loggedInAccount: StateFlow<UserAccountEntity?> = repository.getLoggedInAccountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Vault Files & 100GB Storage
    val vaultFiles: StateFlow<List<VaultFileEntity>> = repository.getVaultFiles(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStorageUsedBytes: StateFlow<Long?> = repository.getTotalStorageUsed(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun registerUser(
        phone: String,
        fullName: String,
        password: String,
        recoverySecretName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerAccount(phone, fullName, password, recoverySecretName)
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে")
            }
        }
    }

    fun loginUser(
        phone: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginAccount(phone, password)
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "লগইন ব্যর্থ হয়েছে")
            }
        }
    }

    fun recoverPassword(
        phone: String,
        secretName: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recoverPasswordWithSecretName(phone, secretName, newPassword)
            result.onSuccess {
                onSuccess()
            }.onFailure { err ->
                onError(err.message ?: "পাসওয়ার্ড উদ্ধার ব্যর্থ হয়েছে")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun uploadFileToVault(
        uri: Uri,
        fileName: String,
        mimeType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    onError("ফাইল পড়া সম্ভব হয়নি")
                    return@launch
                }

                val currentPhone = loggedInAccount.value?.phoneNumber ?: "guest_user"
                val res = repository.saveFileToVault(
                    userPhone = currentPhone,
                    fileName = fileName,
                    mimeType = mimeType,
                    inputStream = inputStream
                )
                res.onSuccess {
                    onSuccess()
                }.onFailure { err ->
                    onError(err.message ?: "ফাইল সংরক্ষণ ব্যর্থ হয়েছে")
                }
            } catch (e: Exception) {
                onError(e.message ?: "ফাইল আপলোড করতে সমস্যা হয়েছে")
            }
        }
    }

    fun deleteVaultFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteVaultFile(fileId)
        }
    }

    fun toggleFavoriteFile(fileId: String) {
        viewModelScope.launch {
            repository.toggleFavoriteFile(fileId)
        }
    }
}
