package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.ui.BasharAiViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AppDrawer
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CanvasScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodeStudioScreen
import com.example.ui.screens.LiveVoiceScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StorageVaultScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.theme.BasharAiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: BasharAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.preferences.collectAsState()

            BasharAiTheme(darkTheme = preferences.isDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: BasharAiViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentScreen by viewModel.currentScreen.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val activeConversationId by viewModel.currentConversationId.collectAsState()

    // Android BackHandler
    BackHandler(enabled = drawerState.isOpen || currentScreen !is ScreenDestination.Chat) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen !is ScreenDestination.Chat) {
            viewModel.navigateTo(ScreenDestination.Chat)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen is ScreenDestination.Chat,
        drawerContent = {
            AppDrawer(
                currentScreen = currentScreen,
                conversations = conversations,
                activeConversationId = activeConversationId,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                },
                onSelectConversation = { id ->
                    viewModel.selectConversation(id)
                },
                onDeleteConversation = { id ->
                    viewModel.deleteConversation(id)
                },
                onNewChat = {
                    viewModel.startNewChat()
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (currentScreen) {
            is ScreenDestination.Chat -> {
                ChatScreen(
                    viewModel = viewModel,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.ToolsHub -> {
                ToolsHubScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Canvas -> {
                CanvasScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.CodeStudio -> {
                CodeStudioScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Calculator -> {
                CalculatorScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.LiveVoice -> {
                LiveVoiceScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Projects -> {
                ProjectsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Memory -> {
                MemoryScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.StorageVault -> {
                StorageVaultScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is ScreenDestination.Auth -> {
                AuthScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(ScreenDestination.Chat) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
