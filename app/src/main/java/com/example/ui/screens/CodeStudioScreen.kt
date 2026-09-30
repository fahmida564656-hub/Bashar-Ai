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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BasharAiViewModel
import com.example.ui.components.FormattedTextContent
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent

val PROGRAMMING_LANGUAGES = listOf("Kotlin", "Python", "JavaScript", "HTML/CSS", "Java", "C++", "SQL", "Bash")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioScreen(
    viewModel: BasharAiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf("Kotlin") }
    var codeSnippet by remember {
        mutableStateOf(
            """
            fun main() {
                val greeting = "বাশার এ আই কোড স্টুডিওতে স্বাগতম!"
                println(greeting)
            }
            """.trimIndent()
        )
    }
    var codeResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "কোড স্টুডিও (Code Studio)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandCyanPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_code_studio")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Language Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PROGRAMMING_LANGUAGES) { lang ->
                    val isSelected = lang == selectedLang
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) BrandCyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            0.8.dp,
                            if (isSelected) BrandCyanPrimary else Color.Transparent
                        ),
                        modifier = Modifier.clickable { selectedLang = lang }
                    ) {
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Code Editor Box
            OutlinedTextField(
                value = codeSnippet,
                onValueChange = { codeSnippet = it },
                placeholder = { Text("এখানে আপনার কোড লিখুন বা পেস্ট করুন...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandCyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0A0F1D)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Code Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isAnalyzing = true
                        codeResult = null
                        val prompt = "Language: $selectedLang\nনিচের কোডটি বিশ্লেষণ করে কোনো বাগ, এরর বা লজিক্যাল সমস্যা আছে কিনা তা স্পষ্টভাবে ধরিয়ে দিন এবং সংশোধিত কোড দিন:\n\n```$selectedLang\n$codeSnippet\n```"
                        viewModel.processCanvasAiAction("CUSTOM", prompt) {
                            isAnalyzing = false
                            codeResult = it
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ডিবাগ (Fix)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isAnalyzing = true
                        codeResult = null
                        val prompt = "Language: $selectedLang\nনিচের কোডটির অ্যালগরিদম ও প্রতিটি লাইনের অর্থ সহজ বাংলায় ব্যাখ্যা করুন:\n\n```$selectedLang\n$codeSnippet\n```"
                        viewModel.processCanvasAiAction("CUSTOM", prompt) {
                            isAnalyzing = false
                            codeResult = it
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandCyanPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ব্যাখ্যা (Explain)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        isAnalyzing = true
                        codeResult = null
                        val prompt = "Language: $selectedLang\nনিচের কোডটিকে আরো দ্রুত ও মেমোরি ইফিশিয়েন্ট করার জন্য অপটিমাইজ করুন:\n\n```$selectedLang\n$codeSnippet\n```"
                        viewModel.processCanvasAiAction("CUSTOM", prompt) {
                            isAnalyzing = false
                            codeResult = it
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandEmeraldAccent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("গতি বৃদ্ধি", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandCyanPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এআই কোড পর্যালোচনা করছে...", style = MaterialTheme.typography.bodySmall, color = BrandCyanPrimary)
                }
            }

            // Code Analysis Result
            codeResult?.let { res ->
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
                                text = "💡 এআই বিশ্লেষণ ও সংশোধিত রূপ:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandEmeraldAccent
                            )

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("code_result", res))
                                Toast.makeText(context, "অনুলিপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(15.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FormattedTextContent(text = res)
                    }
                }
            }
        }
    }
}
