package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ConversationEntity
import com.example.ui.ScreenDestination
import com.example.ui.theme.BrandCyanPrimary
import com.example.ui.theme.BrandEmeraldAccent
import com.example.ui.theme.BrandGoldAccent

@Composable
fun AppDrawer(
    currentScreen: ScreenDestination,
    conversations: List<ConversationEntity>,
    activeConversationId: String?,
    onNavigate: (ScreenDestination) -> Unit,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    ModalDrawerSheet(
        modifier = Modifier.width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {

            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF070D18),
                                Color(0xFF0F1E36)
                            )
                        )
                    )
            ) {
                // Banner background drawable if available
                Image(
                    painter = painterResource(id = R.drawable.bashar_ai_banner),
                    contentDescription = "Bashar AI Banner",
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.45f
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.bashar_ai_icon),
                            contentDescription = "Bashar Ai Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bashar Ai",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Bashar Gojol Studio",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandGoldAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“আপনার প্রশ্ন, আপনার সহকারী, এক জায়গায় AI।”",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // New Chat Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("drawer_new_chat_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandCyanPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("নতুন চ্যাট শুরু করুন", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Primary Navigation Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    DrawerNavSectionTitle("প্রধান ফিচারসমূহ")
                }

                item {
                    DrawerItem(
                        title = "চ্যাট সহকারী",
                        icon = Icons.AutoMirrored.Filled.Chat,
                        isSelected = currentScreen is ScreenDestination.Chat,
                        onClick = {
                            onNavigate(ScreenDestination.Chat)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "১০০ GB ফাইল ভল্ট",
                        icon = Icons.Default.CloudDone,
                        badge = "100 GB",
                        isSelected = currentScreen is ScreenDestination.StorageVault,
                        onClick = {
                            onNavigate(ScreenDestination.StorageVault)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "লগইন / সাইন আপ",
                        icon = Icons.Default.AccountCircle,
                        isSelected = currentScreen is ScreenDestination.Auth,
                        onClick = {
                            onNavigate(ScreenDestination.Auth)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "এআই ক্যালকুলেটর (গণিত)",
                        icon = Icons.Default.Calculate,
                        isSelected = currentScreen is ScreenDestination.Calculator,
                        onClick = {
                            onNavigate(ScreenDestination.Calculator)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "লাইভ ভয়েস মোড",
                        icon = Icons.Default.Mic,
                        badge = "লাইভ",
                        isSelected = currentScreen is ScreenDestination.LiveVoice,
                        onClick = {
                            onNavigate(ScreenDestination.LiveVoice)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "প্রজেক্ট / ওয়ার্কস্পেস",
                        icon = Icons.Default.Folder,
                        isSelected = currentScreen is ScreenDestination.Projects,
                        onClick = {
                            onNavigate(ScreenDestination.Projects)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerItem(
                        title = "এআই মেমোরি ও গোপনীয়তা",
                        icon = Icons.Default.Psychology,
                        isSelected = currentScreen is ScreenDestination.Memory,
                        onClick = {
                            onNavigate(ScreenDestination.Memory)
                            onCloseDrawer()
                        }
                    )
                }

                item {
                    DrawerNavSectionTitle("সাম্প্রতিক কথোপকথন")
                }

                if (conversations.isEmpty()) {
                    item {
                        Text(
                            text = "কোনো সংরক্ষিত কথোপকথন নেই",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    items(conversations) { conv ->
                        val isSelected = conv.id == activeConversationId && currentScreen is ScreenDestination.Chat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) BrandCyanPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    onSelectConversation(conv.id)
                                    onNavigate(ScreenDestination.Chat)
                                    onCloseDrawer()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = conv.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { onDeleteConversation(conv.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // YouTube Channel Promo Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E0E14),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/@BasharGojolStudio")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Safe fallback
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "YouTube",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bashar Gojol Studio",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "@BasharGojolStudio • চ্যানেলটি সাবস্ক্রাইব করুন",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            }

            // Bottom Settings Item
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            DrawerItem(
                title = "সেটিংস ও এআই মডেল",
                icon = Icons.Default.Settings,
                isSelected = currentScreen is ScreenDestination.Settings,
                onClick = {
                    onNavigate(ScreenDestination.Settings)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun DrawerNavSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun DrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurface
                )
                if (badge != null) {
                    Surface(
                        color = BrandEmeraldAccent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) BrandCyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = BrandCyanPrimary.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.padding(vertical = 2.dp)
    )
}
