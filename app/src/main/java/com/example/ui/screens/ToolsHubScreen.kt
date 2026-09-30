package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiToolItem
import com.example.data.model.ChatMode
import com.example.ui.BasharAiViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.FormattedTextContent
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import kotlinx.coroutines.launch

val AI_TOOLS_LIST = listOf(
    AiToolItem(
        id = "gojol_script",
        titleBn = "ইউটিউব ও গজল স্ক্রিপ্ট",
        titleEn = "YouTube & Gojol Studio",
        descriptionBn = "বাশার গজল স্টুডিওর জন্য নতুন আকর্ষণীয় গজল, না’ত ও ভিডিও স্ক্রিপ্ট তৈরি",
        iconEmoji = "🎬",
        category = "Creative",
        defaultPrompt = "বাশার গজল স্টুডিও চ্যানেলের জন্য একটি ভিডিও স্ক্রিপ্ট ও হৃদয়স্পর্শী গজলের কলি প্রস্তুত করো। বিষয়: ",
        inputPlaceholder = "গজলের বিষয় বা সুরের ভাবনা লিখুন..."
    ),
    AiToolItem(
        id = "translator",
        titleBn = "বহুভাষিক অনুবাদক",
        titleEn = "Multilingual Translator",
        descriptionBn = "বাংলা, আরবি, ইংরেজি, উর্দু ও হিন্দিতে নির্ভুল ভাবানুবাদ",
        iconEmoji = "🌐",
        category = "Language",
        defaultPrompt = "নিচের বক্তব্যটি অর্থ ও প্রাঞ্জলতা অক্ষুণ্ণ রেখে চমৎকারভাবে অনুবাদ করুন:\n\n",
        inputPlaceholder = "যে লেখা অনুবাদ করতে চান তা পেস্ট করুন..."
    ),
    AiToolItem(
        id = "summarizer",
        titleBn = "নিবন্ধ ও টেক্সট সারসংক্ষেপ",
        titleEn = "Text Summarizer",
        descriptionBn = "বড় কোনো প্যারাগ্রাফ বা ডকুমেন্ট থেকে মূল পয়েন্ট বের করুন",
        iconEmoji = "📝",
        category = "Productivity",
        defaultPrompt = "নিচের লেখার মূল বক্তব্য ৩-৫টি পয়েন্টে সংক্ষেপে ব্যাখ্যা করুন:\n\n",
        inputPlaceholder = "যে লেখার সারসংক্ষেপ চান তা এখানে দিন..."
    ),
    AiToolItem(
        id = "grammar",
        titleBn = "ব্যাকরণ ও বানান শোধক",
        titleEn = "Grammar & Spell Fixer",
        descriptionBn = "বাংলা ও ইংরেজি বাক্যের ভুল সংশোধন ও মার্জিত রূপান্তর",
        iconEmoji = "✍️",
        category = "Writing",
        defaultPrompt = "নিচের বাক্যে কোনো ব্যাকরণগত বা বানানের ভুল থাকলে সংশোধন করে সুন্দর রূপ দিন:\n\n",
        inputPlaceholder = "যাচাই করার বাক্য লিখুন..."
    ),
    AiToolItem(
        id = "email_writer",
        titleBn = "পেশাদার ইমেইল ও আবেদন",
        titleEn = "Email & Letter Writer",
        descriptionBn = "অফিসিয়াল ইমেইল, ছুটির দরখাস্ত বা ফর্মাল চিঠি তৈরি",
        iconEmoji = "📧",
        category = "Professional",
        defaultPrompt = "নিচের বিষয়ের ওপর একটি ফরমাল প্রফেশনাল ইমেইল ড্রাফট করুন:\n\n",
        inputPlaceholder = "ইমেইলের উদ্দেশ্য ও প্রাপক উল্লেখ করুন..."
    ),
    AiToolItem(
        id = "cv_builder",
        titleBn = "সিভি ও বায়োডাটা প্রস্তুতকারক",
        titleEn = "CV & Resume Builder",
        descriptionBn = "প্রফেশনাল চাকরির উপযোগী আধুনিক বায়োডাটা ফরম্যাট",
        iconEmoji = "📄",
        category = "Professional",
        defaultPrompt = "নিচের তথ্যের ভিত্তিতে একটি আকর্ষণীয় প্রফেশনাল জীবনবৃত্তান্ত (CV Summary) তৈরি করো:\n\n",
        inputPlaceholder = "আপনার পদবি, শিক্ষাগত যোগ্যতা ও অভিজ্ঞতা লিখুন..."
    ),
    AiToolItem(
        id = "study_planner",
        titleBn = "পড়ার রুটিন ও স্টাডি প্ল্যানার",
        titleEn = "Study & Exam Planner",
        descriptionBn = "পরীক্ষা বা স্কিল শেখার জন্য সুনির্দিষ্ট দৈনিক পরিকল্পনা",
        iconEmoji = "📅",
        category = "Education",
        defaultPrompt = "নিচের বিষয়টির জন্য একটি কার্যকর দৈনিক পড়ার রুটিন ও স্টাডি প্ল্যান তৈরি করো:\n\n",
        inputPlaceholder = "পরীক্ষার নাম, অবশিষ্ট দিন ও বিষয়সমূহ লিখুন..."
    ),
    AiToolItem(
        id = "math_solver",
        titleBn = "গণিত ও সমীকরণ সমাধান",
        titleEn = "AI Math Solver",
        descriptionBn = "পাটিগণিত, বীজগণিত বা জ্যামিতিক সমস্যার স্পষ্ট সমাধান",
        iconEmoji = "🧮",
        category = "Education",
        defaultPrompt = "নিচের গাণিতিক সমস্যাটি প্রতিটি ধাপ বিস্তারিত বুঝিয়ে সমাধান করো:\n\n",
        inputPlaceholder = "গাণিতিক প্রশ্নটি লিখুন..."
    ),
    AiToolItem(
        id = "code_assistant",
        titleBn = "কোড সমাধান ও ডিবাগার",
        titleEn = "Code Assistant",
        descriptionBn = "প্রোগ্রামিং কোড লিখুন, ভুল সমাধান করুন ও অপটিমাইজ করুন",
        iconEmoji = "💻",
        category = "Coding",
        defaultPrompt = "নিচের প্রোগ্রামিং সমস্যার জন্য পরিচ্ছন্ন ও কার্যকর কোড প্রদান করো:\n\n",
        inputPlaceholder = "কোন প্রোগ্রামিং ল্যাঙ্গুয়েজ এবং কী তৈরি করতে চান লিখুন..."
    ),
    AiToolItem(
        id = "social_caption",
        titleBn = "ক্যাপশন ও ভাইরাল হ্যাশট্যাগ",
        titleEn = "Caption & Hashtags",
        descriptionBn = "ইউটিউব, ফেসবুক ও ইনস্টাগ্রামের জন্য আকর্ষক ক্যাপশন",
        iconEmoji = "🏷️",
        category = "Marketing",
        defaultPrompt = "নিচের কনটেন্টের জন্য ৫টি ভিন্ন ধরণের সোশ্যাল মিডিয়া ক্যাপশন এবং ট্রেন্ডিং হ্যাশট্যাগ তৈরি করো:\n\n",
        inputPlaceholder = "পোস্ট বা ভিডিওর বিষয় লিখুন..."
    ),
    AiToolItem(
        id = "gojol_lyrics",
        titleBn = "ইসলামিক গজল ও কবিতা",
        titleEn = "Islamic Poetry & Lyrics",
        descriptionBn = "আল্লাহ ও রাসুল (সা.)-এর প্রেমে অন্তর্দৃষ্টিপূর্ণ কবিতার চরণ",
        iconEmoji = "📜",
        category = "Creative",
        defaultPrompt = "বাশার গজল স্টুডিওর ছন্দে ও সুরে একটি প্রাণবন্ত নতুন ইসলামিক না’ত বা গজল রচনা করো। বিষয়: ",
        inputPlaceholder = "গজলের মূল থিম লিখুন..."
    ),
    AiToolItem(
        id = "fact_checker",
        titleBn = "তথ্য যাচাই ও ফ্যাক্ট-চেক",
        titleEn = "Fact Checker",
        descriptionBn = "যেকোনো খবরের সত্যতা, ইতিহাস বা বৈজ্ঞানিক তথ্য বিশ্লেষণ",
        iconEmoji = "🔍",
        category = "Research",
        defaultPrompt = "নিচের তথ্যটির সত্যতা ঐতিহাসিক ও প্রামাণ্য তথ্যের আলোকে যাচাই করো:\n\n",
        inputPlaceholder = "যে তথ্য যাচাই করতে চান তা লিখুন..."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    viewModel: BasharAiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTool by remember { mutableStateOf<AiToolItem?>(null) }
    var userPromptInput by remember { mutableStateOf("") }
    var toolResultText by remember { mutableStateOf<String?>(null) }
    var isRunningTool by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "এআই টুলস হাব",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandCyanPrimary
                        )
                        Text(
                            text = "১২+ বিশেষায়িত সহকারী",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_tools")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(AI_TOOLS_LIST) { tool ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedTool = tool
                            userPromptInput = ""
                            toolResultText = null
                        }
                        .testTag("tool_card_${tool.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandCyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tool.iconEmoji, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = tool.titleBn,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tool.descriptionBn,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Tool Execution
    selectedTool?.let { tool ->
        ModalBottomSheet(
            onDismissRequest = { selectedTool = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tool.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tool.titleBn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tool.titleEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandCyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = userPromptInput,
                    onValueChange = { userPromptInput = it },
                    placeholder = { Text(tool.inputPlaceholder, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandCyanPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (userPromptInput.isNotBlank()) {
                            isRunningTool = true
                            toolResultText = null
                            val fullPrompt = tool.defaultPrompt + userPromptInput
                            viewModel.processCanvasAiAction("CUSTOM", fullPrompt) { res ->
                                isRunningTool = false
                                toolResultText = res
                            }
                        }
                    },
                    enabled = !isRunningTool && userPromptInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRunningTool) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("প্রসেসিং হচ্ছে...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("জেনারেট করুন", fontWeight = FontWeight.Bold)
                    }
                }

                // Result display
                toolResultText?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ফলাফল (Output):",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandEmeraldAccent
                                )

                                Row {
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("tool_output", result))
                                        Toast.makeText(context, "অনুলিপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(15.dp))
                                    }

                                    IconButton(onClick = {
                                        selectedTool = null
                                        viewModel.setChatMode(ChatMode.QUICK)
                                        viewModel.sendMessage(tool.defaultPrompt + userPromptInput)
                                        viewModel.navigateTo(ScreenDestination.Chat)
                                    }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "Open in chat", modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormattedTextContent(text = result)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
