package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMode
import com.example.ui.BasharAiViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.InputBar
import com.example.ui.components.MessageCard
import com.example.ui.components.ResearchProgressCard
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import com.example.ui.theme.BrandGoldAccent
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: BasharAiViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val researchSteps by viewModel.researchSteps.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val suggestedFollowUps by viewModel.suggestedFollowUps.collectAsState()
    val isSpeaking by viewModel.speechHelper.isSpeaking.collectAsState()
    val activeMessageId by viewModel.speechHelper.activeMessageId.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Speech-To-Text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull().orEmpty()
            if (recognizedText.isNotBlank()) {
                inputText = if (inputText.isBlank()) recognizedText else "$inputText $recognizedText"
            }
        }
    }

    fun startVoiceInput() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "বাশার এ আই-কে কিছু বলুন...")
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ভয়েস ইনপুট এই ডিভাইসে সমর্থিত নয়", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bashar Ai",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandCyanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandCyanPrimary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, BrandCyanPrimary)
                            ) {
                                Text(
                                    text = currentMode.labelBn,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = BrandCyanPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Bashar Gojol Studio",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("button_open_drawer")) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.StorageVault) },
                        modifier = Modifier.testTag("button_open_vault_top")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BrandCyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "100GB Vault",
                                tint = BrandCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.LiveVoice) },
                        modifier = Modifier.testTag("button_live_voice")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BrandEmeraldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Live Voice",
                                tint = BrandEmeraldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.startNewChat() },
                        modifier = Modifier.testTag("button_new_chat_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            InputBar(
                inputText = inputText,
                onInputTextChange = { inputText = it },
                currentMode = currentMode,
                onModeChange = { viewModel.setChatMode(it) },
                selectedImage = selectedImage,
                onImageSelected = { viewModel.setSelectedImage(it) },
                onVoiceClick = { startVoiceInput() },
                onSend = {
                    viewModel.sendMessage(it)
                    inputText = ""
                },
                isGenerating = isGenerating,
                isDemoMode = preferences.isDemoMode,
                modifier = Modifier
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                // Landing Hero Screen
                HeroLandingView(
                    currentMode = currentMode,
                    onSelectMode = { viewModel.setChatMode(it) },
                    onQuickPrompt = { prompt, mode ->
                        viewModel.setChatMode(mode)
                        viewModel.sendMessage(prompt)
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(messages) { msg ->
                        MessageCard(
                            message = msg,
                            isSpeaking = isSpeaking && activeMessageId == msg.id,
                            onSpeakToggle = { viewModel.speakMessage(msg.id, msg.content) },
                            onFeedback = { isLiked -> viewModel.updateMessageFeedback(msg.id, isLiked) }
                        )
                    }

                    // Deep research in progress
                    if (isGenerating && currentMode == ChatMode.RESEARCH && researchSteps.isNotEmpty()) {
                        item {
                            ResearchProgressCard(steps = researchSteps)
                        }
                    }

                    // Suggested Follow-up chips
                    if (suggestedFollowUps.isNotEmpty() && !isGenerating) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "💡 সম্পর্কিত পরবর্তী প্রশ্ন (Suggested):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandCyanPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (suggestion in suggestedFollowUps) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = androidx.compose.foundation.BorderStroke(
                                                0.8.dp,
                                                BrandCyanPrimary.copy(alpha = 0.4f)
                                            ),
                                            modifier = Modifier.clickable {
                                                viewModel.sendMessage(suggestion)
                                            }
                                        ) {
                                            Text(
                                                text = suggestion,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HeroLandingView(
    currentMode: ChatMode,
    onSelectMode: (ChatMode) -> Unit,
    onQuickPrompt: (String, ChatMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Icon
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(BrandCyanPrimary.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
                .border(1.5.dp, BrandCyanPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bashar_ai_icon),
                contentDescription = "Bashar AI",
                modifier = Modifier.size(54.dp).clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Bashar Ai",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Smart Ideas • Better Tomorrow",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = BrandGoldAccent
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "“আপনার প্রশ্ন, আপনার সহকারী, এক জায়গায় AI।”",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // YouTube Channel Subscribe Badge
        val context = LocalContext.current
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF260D15),
            border = BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.5f)),
            modifier = Modifier
                .clickable {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/@BasharGojolStudio")
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bashar Gojol Studio • চ্যানেলটি সাবস্ক্রাইব করুন",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = Color(0xFFFF8A80)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Quick Prompt Cards
        Text(
            text = "একটি বিষয় বেছে নিয়ে শুরু করুন:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = BrandCyanPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            QuickPromptCard(
                emoji = "✍️",
                title = "গজল ও সাহিত্য রচনা",
                subtitle = "নতুন ইসলামিক গজল, না’ত ও কবিতার ছন্দ তৈরি করুন",
                onClick = {
                    onQuickPrompt("বাশার গজল স্টুডিওর জন্য একটি অনুপ্রেরণামূলক হৃদয়স্পর্শী ইসলামিক গজলের ৪টি স্তবক লিখে দাও।", ChatMode.WRITING)
                }
            )

            QuickPromptCard(
                emoji = "💻",
                title = "কোডিং ও সমস্যা সমাধান",
                subtitle = "পাইথন, জাভাস্ক্রিপ্ট বা অ্যান্ড্রয়েড কোড লিখুন ও ত্রুটি সংশোধন করুন",
                onClick = {
                    onQuickPrompt("অ্যান্ড্রয়েড জেটপ্যাক কম্পোজে কোরুচিন ও স্টেটফ্লো কীভাবে ব্যবহার করতে হয় সহজ উদাহরণ দিয়ে বুঝিয়ে দাও।", ChatMode.CODING)
                }
            )

            QuickPromptCard(
                emoji = "🔎",
                title = "ডিপ রিসার্চ ও গবেষণা",
                subtitle = "যেকোনো জটিল বিষয়ের বহুমুখী তথ্যসূত্র ও কাঠামোবদ্ধ প্রতিবেদন",
                onClick = {
                    onQuickPrompt("বর্তমান বিশ্বে কৃত্রিম বুদ্ধিমত্তা (AI) প্রযুক্তির ভবিষ্যৎ সম্ভাবনা ও নৈতিক চ্যালেঞ্জের একটি পূর্ণাঙ্গ বিশ্লেষণ তৈরি করো।", ChatMode.RESEARCH)
                }
            )

            QuickPromptCard(
                emoji = "📚",
                title = "পড়াশোনা ও গণিত সমাধান",
                subtitle = "সহজ ভাষায় বিজ্ঞান, ইতিহাস ও গাণিতিক সূত্রের ব্যাখ্যা",
                onClick = {
                    onQuickPrompt("কোয়ান্টাম কম্পিউটিং কী এবং এটি কীভাবে প্রচলিত কম্পিউটারের চেয়ে ভিন্ন, সহজ বাংলায় বুঝিয়ে দাও।", ChatMode.STUDY)
                }
            )
        }
    }
}

@Composable
fun QuickPromptCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
