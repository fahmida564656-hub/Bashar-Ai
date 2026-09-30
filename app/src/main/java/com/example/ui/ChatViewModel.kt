package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.ChatMode
import com.example.data.model.ResearchStep
import com.example.data.model.UserPreferences
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiResult
import com.example.data.repository.BasharAiRepository
import com.example.util.SpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val currentMode: ChatMode = ChatMode.QUICK,
    val selectedImage: Bitmap? = null,
    val researchSteps: List<ResearchStep> = emptyList(),
    val suggestedFollowUps: List<String> = emptyList(),
    val errorMessage: String? = null,
    val currentConversationId: String? = null
)

open class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val repository = BasharAiRepository(application)
    protected val geminiApiClient = GeminiApiClient()
    val speechHelper = SpeechHelper(application)

    // Current Conversation
    protected val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    // Message List State
    protected val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    // Mode
    protected val _currentMode = MutableStateFlow(ChatMode.QUICK)
    val currentMode: StateFlow<ChatMode> = _currentMode.asStateFlow()

    // Multimodal Image Attachment
    protected val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage.asStateFlow()

    // Generating / Loading State
    protected val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Error Message
    protected val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Deep Research Progress
    protected val _researchSteps = MutableStateFlow<List<ResearchStep>>(emptyList())
    val researchSteps: StateFlow<List<ResearchStep>> = _researchSteps.asStateFlow()

    // Suggested Follow-up Questions
    protected val _suggestedFollowUps = MutableStateFlow<List<String>>(emptyList())
    val suggestedFollowUps: StateFlow<List<String>> = _suggestedFollowUps.asStateFlow()

    // Preferences
    protected val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    // Combined UI State
    val uiState: StateFlow<ChatUiState> = combine(
        _messages,
        _isGenerating,
        _currentMode,
        _errorMessage
    ) { msgs, generating, mode, err ->
        ChatUiState(
            messages = msgs,
            isGenerating = generating,
            currentMode = mode,
            selectedImage = _selectedImage.value,
            researchSteps = _researchSteps.value,
            suggestedFollowUps = _suggestedFollowUps.value,
            errorMessage = err,
            currentConversationId = _currentConversationId.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    init {
        // Initialize default or first conversation
        viewModelScope.launch {
            repository.getAllConversations().collect { list ->
                if (list.isNotEmpty() && _currentConversationId.value == null) {
                    selectConversation(list.first().id)
                }
            }
        }
    }

    fun setChatMode(mode: ChatMode) {
        _currentMode.value = mode
    }

    fun setSelectedImage(bitmap: Bitmap?) {
        _selectedImage.value = bitmap
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        viewModelScope.launch {
            repository.getMessagesForConversation(id).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    open fun startNewChat(mode: ChatMode = _currentMode.value) {
        viewModelScope.launch {
            val newId = repository.createConversation(
                title = "নতুন কথোপকথন",
                mode = mode
            )
            _currentMode.value = mode
            _currentConversationId.value = newId
            _messages.value = emptyList()
            _suggestedFollowUps.value = emptyList()
            _researchSteps.value = emptyList()
            _errorMessage.value = null
            selectConversation(newId)
        }
    }

    fun sendMessage(text: String, imageOverride: Bitmap? = null): Job {
        val trimmed = text.trim()
        val imageToProcess = imageOverride ?: _selectedImage.value
        if (trimmed.isEmpty() && imageToProcess == null) {
            return Job().apply { complete() }
        }

        return viewModelScope.launch {
            var convId = _currentConversationId.value
            if (convId == null) {
                convId = repository.createConversation(
                    title = trimmed.take(25).ifBlank { "নথি ও ছবি বিশ্লেষণ" },
                    mode = _currentMode.value
                )
                _currentConversationId.value = convId
                selectConversation(convId)
            }

            _selectedImage.value = null
            _isGenerating.value = true
            _errorMessage.value = null

            // 1. Save User Message to local database
            val userMsg = repository.saveUserMessage(
                conversationId = convId,
                content = trimmed.ifBlank { "ছবি বিশ্লেষণ করুন" },
                imageUri = if (imageToProcess != null) "inline_bitmap" else null
            )
            _messages.value = _messages.value + userMsg

            // 2. Call Gemini API service layer through Repository
            val result = repository.askGemini(
                conversationId = convId,
                prompt = trimmed,
                mode = _currentMode.value,
                imageBitmap = imageToProcess,
                preferences = _preferences.value,
                onResearchStep = { steps ->
                    _researchSteps.value = steps
                }
            )

            _isGenerating.value = false

            // 3. Process API Response
            when (result) {
                is GeminiResult.Success -> {
                    val modelMsg = repository.saveModelMessage(
                        conversationId = convId,
                        content = result.text,
                        sources = result.sources
                    )
                    _messages.value = _messages.value + modelMsg
                    updateSmartSuggestions(_currentMode.value)
                }
                is GeminiResult.Error -> {
                    _errorMessage.value = result.message
                    val errModelMsg = repository.saveModelMessage(
                        conversationId = convId,
                        content = result.message
                    )
                    _messages.value = _messages.value + errModelMsg
                }
            }
        }
    }

    fun regenerateLastResponse() {
        val currentMsgs = _messages.value
        val lastUserMessage = currentMsgs.lastOrNull { it.role == "user" } ?: return
        sendMessage(lastUserMessage.content)
    }

    private fun updateSmartSuggestions(mode: ChatMode) {
        _suggestedFollowUps.value = when (mode) {
            ChatMode.CODING -> listOf(
                "কোডটি কীভাবে কাজ করে ব্যাখ্যা দাও",
                "ত্রুটি হ্যান্ডলিং বা অপটিমাইজেশন যুক্ত করো",
                "একটি বাস্তব প্রয়োগের উদাহরণ দেখাও"
            )
            ChatMode.STUDY, ChatMode.MATH -> listOf(
                "এটা আরও সহজভাবে বুঝিয়ে দাও",
                "ধাপে ধাপে সমাধান দেখাও",
                "একটি বাস্তব উদাহরণ দাও"
            )
            ChatMode.WRITING -> listOf(
                "আরেকটি ভিন্ন আঙ্গিকে লেখো",
                "আরও ছন্দ ও গভীরতা বাড়াও",
                "সংক্ষিপ্ত ও আকর্ষণীয় করো"
            )
            else -> listOf(
                "এ বিষয়ে আরও বিস্তারিত বলো",
                "একটি সহজ উদাহরণ দাও",
                "সারসংক্ষেপ পয়েন্ট আকারে দেখাও"
            )
        }
    }

    fun updateMessageFeedback(messageId: String, isLiked: Boolean?) {
        viewModelScope.launch {
            repository.updateMessageFeedback(messageId, isLiked)
        }
    }

    fun speakMessage(messageId: String, text: String) {
        if (speechHelper.activeMessageId.value == messageId && speechHelper.isSpeaking.value) {
            speechHelper.stop()
        } else {
            speechHelper.speak(text, messageId)
        }
    }

    fun stopSpeaking() {
        speechHelper.stop()
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.shutdown()
    }
}
