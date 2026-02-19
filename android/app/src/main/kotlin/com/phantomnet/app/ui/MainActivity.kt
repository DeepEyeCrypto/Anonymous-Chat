package com.phantomnet.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phantomnet.app.domain.NetworkStatus
import com.phantomnet.app.domain.model.Conversation
import com.phantomnet.app.ui.chat.ChatScreen
import com.phantomnet.app.ui.chat.ChatViewModel
import com.phantomnet.app.ui.discover.DiscoveryScreen
import com.phantomnet.app.ui.home.ConversationListScreen
import com.phantomnet.app.ui.theme.PhantomNetTheme
import com.phantomnet.app.ui.vault.VaultScreen
import com.phantomnet.core.identity.IdentityManager
import com.phantomnet.core.PhantomCore
import com.phantomnet.feature.onboarding.OnboardingScreen
import com.phantomnet.feature.onboarding.OnboardingState
import com.phantomnet.feature.onboarding.SplashScreen
import com.phantomnet.feature.settings.SettingsScreen
import com.phantomnet.feature.settings.SettingsUiState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val allGranted = perms.values.all { it }
                if (!allGranted) {
                    Toast.makeText(context, "Bluetooth permissions required for Mesh", Toast.LENGTH_LONG).show()
                }
            }

            LaunchedEffect(Unit) {
                val missing = permissions.filter {
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                }
                if (missing.isNotEmpty()) launcher.launch(missing.toTypedArray())
            }

            PhantomNetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhantomApp()
                }
            }
        }
    }
}

// ── Tab definitions ──
private enum class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    CHATS("tab_chats", "Chats", Icons.Outlined.Email),
    DISCOVER("tab_discover", "Discover", Icons.Filled.Search),
    VAULT("tab_vault", "Vault", Icons.Filled.Lock),
    SETTINGS("tab_settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun PhantomApp() {
    val context = LocalContext.current
    val identityManager = remember { IdentityManager.getInstance(context) }

    // ── App-level state ──
    var currentScreen by remember { mutableStateOf("splash") }

    // ── Splash state ──
    val splashState = remember {
        if (identityManager.isOnboardingCompleted) OnboardingState.Ready(true)
        else OnboardingState.Ready(false)
    }

    when (currentScreen) {
        "splash" -> {
            SplashScreen(
                splashState = splashState,
                onReady = { hasPersona ->
                    currentScreen = if (hasPersona) "main" else "onboarding"
                }
            )
        }
        "onboarding" -> {
            // Simple onboarding state management
            var creationState by remember {
                mutableStateOf(com.phantomnet.feature.onboarding.IdentityCreationState.Idle
                    as com.phantomnet.feature.onboarding.IdentityCreationState)
            }

            OnboardingScreen(
                creationState = creationState,
                onGenerateIdentity = {
                    creationState = com.phantomnet.feature.onboarding.IdentityCreationState.Generating
                    // Launch identity creation
                    MainScope().launch {
                        try {
                            val persona = identityManager.createPersona()
                            creationState = com.phantomnet.feature.onboarding.IdentityCreationState.Created(
                                persona.displayFingerprint
                            )
                        } catch (t: Throwable) {
                            creationState = com.phantomnet.feature.onboarding.IdentityCreationState.Error(
                                t.message ?: "Failed"
                            )
                        }
                    }
                },
                onSkip = { currentScreen = "main" },
                onComplete = { currentScreen = "main" }
            )
        }
        "main" -> {
            MainShell(
                identityManager = identityManager,
                onWipeComplete = { currentScreen = "onboarding" }
            )
        }
    }
}

@Composable
private fun MainShell(
    identityManager: IdentityManager,
    onWipeComplete: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if we should show bottom bar
    val showBottomBar = currentRoute in Tab.entries.map { it.route }

    Scaffold(
        containerColor = Color(0xFF0B0E11),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1C1F26),
                    contentColor = Color(0xFF00E676)
                ) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(tab.icon, contentDescription = tab.label)
                            },
                            label = {
                                Text(
                                    tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (currentRoute == tab.route) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00E676),
                                selectedTextColor = Color(0xFF00E676),
                                unselectedIconColor = Color(0xFF8B949E),
                                unselectedTextColor = Color(0xFF8B949E),
                                indicatorColor = Color(0xFF00E676).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Network status (existing)
        val torStatus by NetworkStatus.torStatus.collectAsState()
        val dhtStatus by NetworkStatus.dhtStatus.collectAsState()
        val meshStatus by NetworkStatus.meshStatus.collectAsState()

        // Mock conversations (will be replaced with DB in next task)
        val conversations = remember {
            listOf(
                Conversation(
                    id = "1",
                    contactName = "Alice (Tor)",
                    lastMessage = "Signal handshake complete. Identity verified.",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    unreadCount = 2,
                    isOnline = true
                ),
                Conversation(
                    id = "2",
                    contactName = "Bob (DHT)",
                    lastMessage = "Can you send the architectural blueprints?",
                    lastMessageTimestamp = System.currentTimeMillis() - 3600000,
                    unreadCount = 0,
                    isOnline = false
                )
            )
        }

        NavHost(
            navController = navController,
            startDestination = Tab.CHATS.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ── Tab: Chats ──
            composable(Tab.CHATS.route) {
                ConversationListScreen(
                    conversations = conversations,
                    torStatus = torStatus,
                    dhtStatus = dhtStatus,
                    meshStatus = meshStatus,
                    onConversationClick = { id -> navController.navigate("chat/$id") },
                    onRoomClick = { name -> navController.navigate("room/$name") },
                    onFabClick = { /* future: new chat */ }
                )
            }

            // ── Tab: Discover ──
            composable(Tab.DISCOVER.route) {
                DiscoveryScreen()
            }

            // ── Tab: Vault ──
            composable(Tab.VAULT.route) {
                VaultScreen()
            }

            // ── Tab: Settings ──
            composable(Tab.SETTINGS.route) {
                val settingsState = remember {
                    SettingsUiState(
                        fingerprint = identityManager.fingerprint?.let { fp ->
                            fp.chunked(2).joinToString(" ").uppercase()
                        } ?: "Not created",
                        coreAvailable = PhantomCore.isAvailable,
                        appVersion = "2.0.0-alpha"
                    )
                }

                SettingsScreen(
                    state = settingsState,
                    onPrivacyDashboardClick = { navController.navigate("privacy") },
                    onSecureBackupClick = { navController.navigate("backup") },
                    onWipeConfirmed = {
                        MainScope().launch {
                            identityManager.wipeAll()
                            onWipeComplete()
                        }
                    }
                )
            }

            // ── Detail screens (no bottom bar) ──
            composable("chat/{conversationId}") { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                val conversation = conversations.find { it.id == conversationId }
                val chatViewModel: ChatViewModel = viewModel()
                val messages by chatViewModel.messages.collectAsState()

                if (conversation != null) {
                    ChatScreen(
                        contactName = conversation.contactName,
                        messages = messages,
                        onSendMessage = { text ->
                            chatViewModel.sendMessage(text, "mock_recipient_key")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable("room/{roomName}") { backStackEntry ->
                val roomName = backStackEntry.arguments?.getString("roomName") ?: "Secret Room"
                com.phantomnet.app.ui.rooms.DcNetRoomScreen(
                    roomName = roomName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("privacy") {
                com.phantomnet.app.ui.privacy.PrivacyDashboardScreen(
                    onBackClick = { navController.popBackStack() },
                    onBackupClick = { navController.navigate("backup") }
                )
            }

            composable("backup") {
                com.phantomnet.app.ui.backup.ShardWizardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
