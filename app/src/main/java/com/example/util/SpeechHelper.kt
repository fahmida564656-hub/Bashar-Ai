package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _activeMessageId = MutableStateFlow<String?>(null)
    val activeMessageId: StateFlow<String?> = _activeMessageId.asStateFlow()

    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val localeBn = Locale.forLanguageTag("bn-BD")
                val res = tts?.setLanguage(localeBn)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.ENGLISH
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeMessageId.value = null
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _activeMessageId.value = null
                    }
                })
            }
        }
    }

    fun speak(text: String, messageId: String? = null) {
        if (!isInitialized || tts == null) return
        stop()

        // Clean markdown symbols for natural TTS speech
        val cleanedText = text
            .replace(Regex("[#*`_\\[\\]()]"), "")
            .take(1500)

        _activeMessageId.value = messageId
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId ?: "tts_id")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _activeMessageId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
