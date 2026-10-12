package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.manual.ManualAddFoodDialog
import com.example.ui.settings.SettingsDialog
import com.example.ui.suggestions.MealSuggestionsScreen
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.voice.VoiceLogSheet
import kotlinx.coroutines.launch

import com.example.data.repository.NutritionRepository

enum class AppScreen {
    DASHBOARD,
    SUGGESTIONS,
    HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriStepsApp(viewModel: NutriStepsViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    val nutrientState by viewModel.nutrientState.collectAsStateWithLifecycle()
    val todayFoods by viewModel.todayFoods.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val recentStats by viewModel.recentStats.collectAsStateWithLifecycle()
    val macroSummaries by viewModel.macroSummaries.collectAsStateWithLifecycle()

    // Voice log state
    val voiceTranscript by viewModel.voiceTranscript.collectAsStateWithLifecycle()
    val isAnalyzingVoice by viewModel.isAnalyzingVoice.collectAsStateWithLifecycle()
    val parsedAnalysis by viewModel.parsedAnalysis.collectAsStateWithLifecycle()
    val voiceErrorMessage by viewModel.voiceErrorMessage.collectAsStateWithLifecycle()

    // Suggestions state
    val suggestedMeals by viewModel.suggestedMeals.collectAsStateWithLifecycle()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsStateWithLifecycle()
    val suggestionMessage by viewModel.suggestionMessage.collectAsStateWithLifecycle()

    // Notifications
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val syncNotification by viewModel.syncNotification.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessage()
        }
    }

    LaunchedEffect(syncNotification) {
        syncNotification?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncNotification()
        }
    }

    // Modal Sheet & Dialogs State
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(EmeraldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncHealthPlatform() },
                        modifier = Modifier.testTag("topbar_sync_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Health",
                            tint = EmeraldPrimary
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("topbar_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { currentScreen = AppScreen.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SUGGESTIONS,
                    onClick = { currentScreen = AppScreen.SUGGESTIONS },
                    icon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suggestions")
                    },
                    label = { Text("Suggestions") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_suggestions")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.HISTORY,
                    onClick = { currentScreen = AppScreen.HISTORY },
                    icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Trends") },
                    label = { Text("Trends") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.DASHBOARD) {
                ExtendedFloatingActionButton(
                    onClick = { showVoiceSheet = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("global_voice_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Log",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Log",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        state = nutrientState,
                        todayFoods = todayFoods,
                        onShiftDate = { delta -> viewModel.shiftDate(delta) },
                        onResetDateToToday = { viewModel.setSelectedDate(NutritionRepository.getTodayDateString()) },
                        onOpenVoiceSheet = { showVoiceSheet = true },
                        onOpenManualAdd = { showManualAddDialog = true },
                        onOpenSettings = { showSettingsDialog = true },
                        onSyncSteps = { viewModel.syncHealthPlatform() },
                        onAddSteps = { amount -> viewModel.addManualSteps(amount) },
                        onUpdateWater = { delta -> viewModel.updateWaterIntake(delta) },
                        onDeleteFood = { food -> viewModel.deleteFoodItem(food) },
                        onOpenSuggestions = { currentScreen = AppScreen.SUGGESTIONS }
                    )
                }
                AppScreen.SUGGESTIONS -> {
                    MealSuggestionsScreen(
                        state = nutrientState,
                        suggestions = suggestedMeals,
                        isLoading = isLoadingSuggestions,
                        suggestionMessage = suggestionMessage,
                        onRequestSuggestions = { mealType -> viewModel.loadPersonalizedMealSuggestions(mealType) },
                        onLogSuggestedMeal = { meal ->
                            viewModel.logSuggestedMeal(meal)
                            currentScreen = AppScreen.DASHBOARD
                        }
                    )
                }
                AppScreen.HISTORY -> {
                    HistoryScreen(
                        recentStats = recentStats,
                        macroSummaries = macroSummaries,
                        calorieGoal = userPreferences.dailyCalorieTarget,
                        stepGoal = userPreferences.dailyStepGoal,
                        onSelectDate = { date ->
                            viewModel.setSelectedDate(date)
                            currentScreen = AppScreen.DASHBOARD
                        }
                    )
                }
            }
        }
    }

    // Voice Log Bottom Sheet
    if (showVoiceSheet) {
        VoiceLogSheet(
            sheetState = sheetState,
            voiceTranscript = voiceTranscript,
            isAnalyzing = isAnalyzingVoice,
            parsedAnalysis = parsedAnalysis,
            errorMessage = voiceErrorMessage,
            onTranscriptChange = { text -> viewModel.setVoiceTranscript(text) },
            onAnalyzeTranscript = { mealType -> viewModel.analyzeVoiceTranscript(mealType) },
            onConfirmLog = { mealType ->
                viewModel.confirmAndLogParsedAnalysis(mealType)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showVoiceSheet = false
                }
            },
            onDismiss = {
                viewModel.clearVoiceState()
                showVoiceSheet = false
            }
        )
    }

    // Manual Add Food Dialog
    if (showManualAddDialog) {
        ManualAddFoodDialog(
            initialMealType = "Lunch",
            onAddFood = { name, grams, cal, prot, carbs, fat, fiber, mealType ->
                viewModel.logDirectFood(
                    name = name,
                    weightGrams = grams,
                    calories = cal,
                    proteinG = prot,
                    carbsG = carbs,
                    fatG = fat,
                    fiberG = fiber,
                    mealType = mealType
                )
            },
            onDismiss = { showManualAddDialog = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            preferences = userPreferences,
            onSave = { cal, prot, carbs, fat, steps, weight, eatBack, platform ->
                viewModel.updatePreferences(cal, prot, carbs, fat, steps, weight, eatBack, platform)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
