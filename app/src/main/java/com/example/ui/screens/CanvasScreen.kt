package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BasharAiViewModel
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CanvasScreen(
    viewModel: BasharAiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canvasDocs by viewModel.canvasDocs.collectAsState()
    val activeDoc by viewModel.activeCanvasDoc.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var docTitle by remember(activeDoc) { mutableStateOf(activeDoc?.title.orEmpty()) }
    var docContent by remember(activeDoc) { mutableStateOf(activeDoc?.content.orEmpty()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ক্যানভাস স্টুডিও (AI Canvas)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandCyanPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_canvas")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.loadCanvasDoc(null)
                            docTitle = ""
                            docContent = ""
                        },
                        modifier = Modifier.testTag("button_new_canvas_doc")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Doc")
                    }

                    IconButton(
                        onClick = {
                            viewModel.saveCanvasDoc(docTitle, docContent)
                            Toast.makeText(context, "খসড়া সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("button_save_canvas_doc")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = BrandEmeraldAccent)
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
        ) {
            // Document Title
            OutlinedTextField(
                value = docTitle,
                onValueChange = { docTitle = it },
                placeholder = { Text("নথির শিরোনাম লিখুন (যেমন: নতুন গজলের ভাবনা)...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandCyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Action Quick Buttons
            Text(
                text = "✨ এআই অ্যাকশন (AI Actions):",
                style = MaterialTheme.typography.labelSmall,
                color = BrandCyanPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CanvasActionChip("🔄 পুনর্লিখন (Rewrite)") {
                    viewModel.processCanvasAiAction("REWRITE", docContent) { docContent = it }
                }
                CanvasActionChip("✂️ সংক্ষেপ করো (Shorten)") {
                    viewModel.processCanvasAiAction("SHORTEN", docContent) { docContent = it }
                }
                CanvasActionChip("📖 বিস্তারিত করো (Expand)") {
                    viewModel.processCanvasAiAction("EXPAND", docContent) { docContent = it }
                }
                CanvasActionChip("🌐 ইংরেজিতে রূপান্তর (English)") {
                    viewModel.processCanvasAiAction("TRANSLATE_EN", docContent) { docContent = it }
                }
                CanvasActionChip("🇧🇩 বাংলায় অনুবাদ (Bangla)") {
                    viewModel.processCanvasAiAction("TRANSLATE_BN", docContent) { docContent = it }
                }
                CanvasActionChip("📋 কপি করো") {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("canvas", docContent))
                    Toast.makeText(context, "অনুলিপি সম্পন্ন", Toast.LENGTH_SHORT).show()
                }
                CanvasActionChip("📤 শেয়ার করো") {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, docTitle + "\n\n" + docContent)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                }
            }

            if (isGenerating) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BrandCyanPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("এআই লিখছে...", style = MaterialTheme.typography.bodySmall, color = BrandCyanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Editor Field
            OutlinedTextField(
                value = docContent,
                onValueChange = { docContent = it },
                placeholder = {
                    Text(
                        "এখানে আপনার প্রবন্ধ, গজল, নিবন্ধ বা যেকোনো ভাবনা লিখুন। উপরে থাকা এআই বাটনের সাহায্যে সরাসরি লেখার মান উন্নত করতে পারবেন...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandCyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun CanvasActionChip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(0.6.dp, BrandCyanPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
