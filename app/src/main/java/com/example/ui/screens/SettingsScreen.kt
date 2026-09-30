package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AiPersonality
import com.example.data.model.PreferredLanguage
import com.example.data.model.ResponseStyle
import com.example.ui.BasharAiViewModel
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import com.example.ui.theme.BrandGoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BasharAiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences by viewModel.preferences.collectAsState()

    var apiKeyInput by remember(preferences.customApiKey) { mutableStateOf(preferences.customApiKey) }
    var showApiKeyField by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "সেটিংস ও এআই মডেল",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandCyanPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_settings")) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Brand Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BrandCyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bashar_ai_icon),
                            contentDescription = "Logo",
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "বাশার এ আই — Bashar AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bashar Gojol Studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandGoldAccent
                        )
                        Text(
                            text = "“আপনার প্রশ্ন, আপনার সহকারী, এক জায়গায় AI।”",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // AI Model Router Section
            SettingsSectionHeader(title = "🤖 এআই মডেল রাউটার (Model Router)", icon = Icons.Default.Router)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val models = listOf(
                        "auto" to "স্বয়ংক্রিয় স্মার্ট রাউটার (Auto Smart Router)",
                        "gemini-3.5-flash" to "Gemini 3.5 Flash (দ্রুত ও নিখুঁত)",
                        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro (উচ্চতর যুক্তি ও কোডিং)"
                    )
                    for ((modelKey, modelTitle) in models) {
                        val isSelected = preferences.selectedModel == modelKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updatePreferences(preferences.copy(selectedModel = modelKey))
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = modelTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = BrandCyanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Development Mode (Section 51)
            SettingsSectionHeader(title = "⚙️ অপারেশন মোড (Development Mode)", icon = Icons.Default.Info)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (preferences.isDemoMode) "ডেমো মোড (Demo Mode)" else "প্রোডাকশন লাইভ মোড (Production)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (preferences.isDemoMode) MaterialTheme.colorScheme.tertiary else BrandEmeraldAccent
                            )
                            Text(
                                text = if (preferences.isDemoMode) "অফলাইন/ডেমো রেসপন্স ব্যবহার করছে।" else "Gemini API ক্লাউড মডেলের মাধ্যমে সরাসরি সংযুক্ত।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = !preferences.isDemoMode,
                            onCheckedChange = { isProd ->
                                viewModel.updatePreferences(preferences.copy(isDemoMode = !isProd))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandEmeraldAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // API Key Config
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showApiKeyField = !showApiKeyField }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = BrandCyanPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showApiKeyField) "কাস্টম API Key লুকান" else "কাস্টম API Key কনফিগার করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandCyanPrimary
                        )
                    }

                    if (showApiKeyField) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("Gemini API Key দিন (যেমন: AIzaSy...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandCyanPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                viewModel.updatePreferences(preferences.copy(customApiKey = apiKeyInput.trim()))
                                Toast.makeText(context, "API Key সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyanPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Web Search Grounding (Section 07)
            SettingsSectionHeader(title = "🌐 রিয়েল-টাইম তথ্যসূত্র (Web Search & Sources)", icon = Icons.Default.Public)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "তথ্যসূত্র সংযোগ সক্রিয় রাখুন",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "উত্তরের সাথে বিশ্বস্ত উৎস কার্ড ও সাইটেশন প্রদর্শন করবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.webSearchEnabled,
                        onCheckedChange = { viewModel.updatePreferences(preferences.copy(webSearchEnabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandCyanPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Personality Settings
            SettingsSectionHeader(title = "🧠 এআই ব্যক্তিত্ব (Personality)", icon = Icons.Default.Psychology)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    for (p in AiPersonality.values()) {
                        val isSelected = preferences.personality == p
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updatePreferences(preferences.copy(personality = p)) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.titleBn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BrandCyanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Response Style
            SettingsSectionHeader(title = "📏 উত্তরের ধরন (Response Style)", icon = Icons.Default.Style)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    for (style in ResponseStyle.values()) {
                        val isSelected = preferences.responseStyle == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updatePreferences(preferences.copy(responseStyle = style)) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${style.titleBn} (${style.titleEn})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BrandCyanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Clear All Data (Section 37 Privacy)
            Button(
                onClick = { showClearDataDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সকল চ্যাট ও ডাটা স্থায়ীভাবে মুছুন (Delete My Data)")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("সকল ডাটা মুছে ফেলতে নিশ্চিত?") },
            text = { Text("এটি বাশার এআই-এর সমস্ত সংরক্ষিত কথোপকথন, বার্তা ও স্মৃতি ডিভাইস থেকে স্থায়ীভাবে মুছে দেবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "সকল ডাটা মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BrandCyanPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
