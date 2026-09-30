package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.VaultFileEntity
import com.example.ui.BasharAiViewModel
import com.example.ui.ScreenDestination
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import com.example.ui.theme.BrandGoldAccent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TOTAL_CAPACITY_BYTES = 100L * 1024L * 1024L * 1024L // 100 GB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageVaultScreen(
    viewModel: BasharAiViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val loggedInAccount by viewModel.loggedInAccount.collectAsState()
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val totalStorageUsedBytes by viewModel.totalStorageUsedBytes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var fileToDelete by remember { mutableStateOf<VaultFileEntity?>(null) }
    var fileToPreview by remember { mutableStateOf<VaultFileEntity?>(null) }

    // File Pickers
    val anyFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileDetails = getUriFileNameAndType(context, uri)
            viewModel.uploadFileToVault(
                uri = uri,
                fileName = fileDetails.first,
                mimeType = fileDetails.second,
                onSuccess = {
                    Toast.makeText(context, "ফাইল সফলভাবে ভল্টে জমা হয়েছে!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileDetails = getUriFileNameAndType(context, uri)
            viewModel.uploadFileToVault(
                uri = uri,
                fileName = fileDetails.first.ifBlank { "photo_${System.currentTimeMillis()}.jpg" },
                mimeType = fileDetails.second.ifBlank { "image/jpeg" },
                onSuccess = {
                    Toast.makeText(context, "ছবি সফলভাবে ভল্টে জমা হয়েছে!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // Filter Files
    val filteredFiles = vaultFiles.filter { file ->
        val matchesCategory = when (selectedCategory) {
            "ALL" -> true
            else -> file.category == selectedCategory
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            file.fileName.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    val usedBytes = totalStorageUsedBytes ?: 0L
    val usedFraction = (usedBytes.toDouble() / TOTAL_CAPACITY_BYTES.toDouble()).coerceIn(0.0, 1.0).toFloat()
    val freeBytes = (TOTAL_CAPACITY_BYTES - usedBytes).coerceAtLeast(0L)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "১০০ GB ফাইল ভল্ট",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (loggedInAccount != null) "ব্যবহারকারী: ${loggedInAccount!!.fullName}" else "ব্যক্তিগত ক্লাউড ড্রাইভ",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandCyanPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_vault_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Auth) },
                        modifier = Modifier.testTag("button_account_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = if (loggedInAccount != null) BrandEmeraldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Storage Overview Card (100 GB Quota Display)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "১০০ GB ক্লাউড স্টোরেজ",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ছবি, ভিডিও, অডিও ও ডকুমেন্টস নিরাপদে জমা রাখুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrandEmeraldAccent.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmeraldAccent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "100 GB ফ্রি",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandEmeraldAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { if (usedFraction < 0.01f && usedBytes > 0) 0.01f else usedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = BrandCyanPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quota details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StorageStatPill(
                                label = "ব্যবহৃত",
                                value = formatFileSize(usedBytes),
                                color = BrandCyanPrimary
                            )
                            StorageStatPill(
                                label = "মোট বরাদ্দ",
                                value = "১০০ GB",
                                color = BrandGoldAccent
                            )
                            StorageStatPill(
                                label = "অবশিষ্ট খালি",
                                value = formatFileSize(freeBytes),
                                color = BrandEmeraldAccent
                            )
                        }
                    }
                }
            }

            // Quick Upload Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { anyFilePicker.launch("*/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("button_upload_any_file"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyanPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ফাইল আপলোড",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("button_upload_photo"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = BrandEmeraldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ছবি / ভিডিও",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("জমা রাখা ফাইলের নাম দিয়ে খুঁজুন...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_vault"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandCyanPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    // Categories
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            CategoryFilterChip(
                                label = "সকল ফাইল (${vaultFiles.size})",
                                isSelected = selectedCategory == "ALL",
                                onClick = { selectedCategory = "ALL" }
                            )
                        }
                        item {
                            CategoryFilterChip(
                                label = "ডকুমেন্টস 📄",
                                isSelected = selectedCategory == "DOCUMENT",
                                onClick = { selectedCategory = "DOCUMENT" }
                            )
                        }
                        item {
                            CategoryFilterChip(
                                label = "ছবি ও ফটো 🖼️",
                                isSelected = selectedCategory == "PHOTO",
                                onClick = { selectedCategory = "PHOTO" }
                            )
                        }
                        item {
                            CategoryFilterChip(
                                label = "অডিও ও গজল 🎵",
                                isSelected = selectedCategory == "AUDIO",
                                onClick = { selectedCategory = "AUDIO" }
                            )
                        }
                        item {
                            CategoryFilterChip(
                                label = "ভিডিও 🎬",
                                isSelected = selectedCategory == "VIDEO",
                                onClick = { selectedCategory = "VIDEO" }
                            )
                        }
                        item {
                            CategoryFilterChip(
                                label = "আর্কাইভ ও অন্যান্য 📦",
                                isSelected = selectedCategory == "ARCHIVE",
                                onClick = { selectedCategory = "ARCHIVE" }
                            )
                        }
                    }
                }
            }

            // File List Section
            if (filteredFiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = BrandCyanPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "কোনো মিল পাওয়া যায়নি" else "ভল্টে এখনও কোনো ফাইল নেই",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উপরের 'ফাইল আপলোড' বাটনে ট্যাপ করে আপনার যেকোনো প্রয়োজনীয় ফাইল, ছবি বা গজল জমা রাখুন। ১০০ GB জায়গা বরাদ্দ রয়েছে।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredFiles, key = { it.id }) { file ->
                    VaultFileRow(
                        file = file,
                        onFavoriteClick = { viewModel.toggleFavoriteFile(file.id) },
                        onShareClick = { shareVaultFile(context, file) },
                        onDeleteClick = { fileToDelete = file },
                        onItemClick = { fileToPreview = file }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("ফাইল মুছে ফেলবেন?") },
            text = { Text("আপনি কি নিশ্চিতভাবে \"${target.fileName}\" ফাইলটি ভল্ট থেকে মুছে ফেলতে চান? এটি মুছে দিলে স্টোরেজ খালি হবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVaultFile(target.id)
                        fileToDelete = null
                        Toast.makeText(context, "ফাইল মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Preview / Details Dialog
    if (fileToPreview != null) {
        val file = fileToPreview!!
        AlertDialog(
            onDismissRequest = { fileToPreview = null },
            title = {
                Text(
                    text = file.fileName,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📁 ধরণ: ${file.category}")
                    Text("📦 আকার: ${formatFileSize(file.fileSize)}")
                    Text("🕒 আপলোড: ${formatDate(file.uploadedAt)}")
                    Text("📍 স্থান: সংরক্ষিত ভল্ট (১০০ GB কোটা)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareVaultFile(context, file)
                        fileToPreview = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyanPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("শেয়ার করুন", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToPreview = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
fun StorageStatPill(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandCyanPrimary.copy(alpha = 0.2f),
            selectedLabelColor = BrandCyanPrimary
        )
    )
}

@Composable
fun VaultFileRow(
    file: VaultFileEntity,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getCategoryColor(file.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(file.category),
                    contentDescription = null,
                    tint = getCategoryColor(file.category),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatFileSize(file.fileSize),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandCyanPrimary
                    )
                    Text(
                        text = " • ${formatDate(file.uploadedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions
            IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (file.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "PHOTO" -> Icons.Default.Image
        "AUDIO" -> Icons.Default.Audiotrack
        "VIDEO" -> Icons.Default.Videocam
        "DOCUMENT" -> Icons.Default.Description
        "ARCHIVE" -> Icons.Default.FolderZip
        else -> Icons.Default.InsertDriveFile
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "PHOTO" -> Color(0xFF00E5FF)
        "AUDIO" -> Color(0xFFFFB300)
        "VIDEO" -> Color(0xFFFF5252)
        "DOCUMENT" -> Color(0xFF00E676)
        "ARCHIVE" -> Color(0xFFE040FB)
        else -> Color(0xFF90CAF9)
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
        bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024.0 * 1024.0))
        bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes.toDouble() / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getUriFileNameAndType(context: Context, uri: Uri): Pair<String, String> {
    var name = "uploaded_file"
    var mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                name = it.getString(nameIndex) ?: name
            }
        }
    }
    return Pair(name, mime)
}

private fun shareVaultFile(context: Context, file: VaultFileEntity) {
    try {
        val f = File(file.localFilePath)
        if (!f.exists()) {
            Toast.makeText(context, "ফাইলটি খুঁজে পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            f
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = file.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "ফাইল শেয়ার করুন"))
    } catch (e: Exception) {
        Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
