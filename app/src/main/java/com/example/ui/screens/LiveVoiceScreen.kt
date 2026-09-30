package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.BasharAiViewModel
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import java.util.Locale

@Composable
fun LiveVoiceScreen(
    viewModel: BasharAiViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSpeaking by viewModel.speechHelper.isSpeaking.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var speechStatus by remember { mutableStateOf("বাশার এ আই প্রস্তুত। কথা বলতে মাইকে চাপুন।") }
    var recognizedQuery by remember { mutableStateOf("") }
    var lastAiReply by remember { mutableStateOf("") }
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordPermission = granted
        if (!granted) {
            Toast.makeText(context, "ভয়েস মোডের জন্য মাইক্রোফোন পারমিশন প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    // Android Speech Recognizer setup
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    var isListening by remember { mutableStateOf(false) }

    fun startListening() {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (speechRecognizer == null) {
            Toast.makeText(context, "ভয়েস রিকগনিশন সমর্থিত নয়", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.stopSpeaking()
        isListening = true
        speechStatus = "শুনছি... আপনার প্রশ্ন বা কথা বলুন"

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                speechStatus = "চিন্তা করছি... (Processing)"
            }

            override fun onError(error: Int) {
                isListening = false
                speechStatus = "কথা শোনা যায়নি। আবার চেষ্টা করুন।"
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    recognizedQuery = text
                    speechStatus = "বাশার এ আই উত্তর তৈরি করছে..."
                    viewModel.sendMessage(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull().orEmpty()
                if (partial.isNotBlank()) {
                    recognizedQuery = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechStatus = "থেমে গেছে"
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            viewModel.stopSpeaking()
        }
    }

    // Observe messages to trigger TTS automatically in Live Mode
    val messages by viewModel.messages.collectAsState()
    LaunchedEffect(messages.size) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg != null && lastMsg.role == "model") {
            lastAiReply = lastMsg.content
            speechStatus = "বাশার এ আই উত্তর দিচ্ছে..."
            viewModel.speakMessage(lastMsg.id, lastMsg.content)
        }
    }

    // Animation transition for pulsing aura
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening || isSpeaking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070D18),
                        Color(0xFF0F1E36),
                        Color(0xFF070D18)
                    )
                )
            )
    ) {
        // Exit button
        IconButton(
            onClick = {
                stopListening()
                viewModel.stopSpeaking()
                onClose()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .testTag("button_close_live_voice")
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "বাশার এ আই — লাইভ মোড",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = "Bashar Gojol Studio",
                style = MaterialTheme.typography.labelMedium,
                color = BrandEmeraldAccent
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Pulsing Glowing Visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            when {
                                isListening -> BrandEmeraldAccent.copy(alpha = 0.25f)
                                isSpeaking -> BrandCyanPrimary.copy(alpha = 0.25f)
                                else -> Color.White.copy(alpha = 0.05f)
                            }
                        )
                )

                // Mid glow ring
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isListening -> BrandEmeraldAccent.copy(alpha = 0.4f)
                                isSpeaking -> BrandCyanPrimary.copy(alpha = 0.4f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                        )
                )

                // Central Mic Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isListening -> BrandEmeraldAccent
                                isSpeaking -> BrandCyanPrimary
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .clickable {
                            if (isListening) stopListening() else startListening()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isListening || isSpeaking) Color.Black else Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Status message
            Text(
                text = speechStatus,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isListening -> BrandEmeraldAccent
                        isSpeaking -> BrandCyanPrimary
                        else -> Color(0xFFCBD5E1)
                    }
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transcription card
            if (recognizedQuery.isNotBlank() || lastAiReply.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132238).copy(alpha = 0.8f)),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, BrandCyanPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (recognizedQuery.isNotBlank()) {
                            Text(
                                text = "🗣️ আপনি: $recognizedQuery",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (lastAiReply.isNotBlank()) {
                            Text(
                                text = "🤖 বাশার এ আই: ${lastAiReply.take(180)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandCyanPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isSpeaking) {
                    Button(
                        onClick = { viewModel.stopSpeaking() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কথা থামান")
                    }
                }

                Button(
                    onClick = {
                        if (isListening) stopListening() else startListening()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color(0xFFEF4444) else BrandCyanPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isListening) "মিউট করুন" else "কথা বলুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
