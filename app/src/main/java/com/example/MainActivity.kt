package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassBottomNavBar
import com.example.ui.components.GlassTopBar
import com.example.ui.screens.calculator.CalculatorScreen
import com.example.ui.screens.kanban.KanbanScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.vendors.VendorsHubScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BakriTheme
import com.example.ui.theme.CrystalBg
import com.example.ui.theme.ObsidianBg
import com.example.viewmodel.AppTab
import com.example.viewmodel.BakriViewModel

import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.security.LockScreen

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application as BakriApplication
            val viewModel = remember { BakriViewModel(app.repository, app.preferences) }
            val themeMode by viewModel.themeMode.collectAsState()
            val isUnlocked by viewModel.isAppUnlocked.collectAsState()
            val pinCode by viewModel.pinCode.collectAsState()
            val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

            BakriTheme(themeMode = themeMode) {
                // Enforce Arabic RTL layout for authentic local signage agency workflow
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    if (!isUnlocked) {
                        LockScreen(
                            expectedPin = pinCode,
                            isBiometricEnabled = isBiometricEnabled,
                            onUnlocked = { viewModel.unlockApp() }
                        )
                    } else {
                        BakriAppContent(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BakriAppContent(viewModel: BakriViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currencyMode by viewModel.currencyMode.collectAsState()
    val isDark = isSystemInDarkTheme()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle back button: return to Calculator tab if on sub-tab
    BackHandler(enabled = currentTab != AppTab.CALCULATOR) {
        viewModel.setTab(AppTab.CALCULATOR)
    }

    // Ambient liquid gradient background
    val ambientBgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF06090F),
                Color(0xFF0D121D),
                Color(0xFF090D15)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9),
                Color(0xFFE2E8F0)
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientBgGradient),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Area with smooth fade transitions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 74.dp) // space for floating top bar
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
                        AppTab.ORDERS -> OrdersScreen(viewModel = viewModel)
                        AppTab.VENDORS -> VendorsHubScreen(viewModel = viewModel)
                        AppTab.KANBAN -> KanbanScreen(viewModel = viewModel)
                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }

            // Floating Frosted Glass Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                GlassTopBar(
                    activeTab = currentTab,
                    themeMode = themeMode,
                    currencyMode = currencyMode,
                    onToggleTheme = {
                        val nextMode = when (themeMode) {
                            AppThemeMode.DARK -> AppThemeMode.LIGHT
                            AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
                            AppThemeMode.SYSTEM -> AppThemeMode.DARK
                        }
                        viewModel.setThemeMode(nextMode)
                    },
                    onToggleCurrency = { viewModel.toggleCurrency() }
                )
            }

            // Floating Frosted Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                GlassBottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        }
    }
}
