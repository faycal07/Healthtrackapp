package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.health.StepTrackingManager
import com.example.data.local.DailyMacroSummary
import com.example.data.local.DailyStatsEntity
import com.example.data.local.FoodLogEntity
import com.example.data.local.NutriStepsDatabase
import com.example.data.local.UserPreferencesEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.ParsedFoodAnalysis
import com.example.data.remote.ParsedFoodItem
import com.example.data.remote.SuggestedMeal
import com.example.data.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardNutrientState(
    val selectedDate: String,
    val baseCalorieGoal: Int = 2200,
    val consumedCalories: Double = 0.0,
    val burnedCalories: Double = 0.0,
    val adjustedCalorieBudget: Double = 2200.0,
    val remainingCalories: Double = 2200.0,
    val netCalories: Double = 0.0,
    val proteinConsumed: Double = 0.0,
    val proteinGoal: Int = 145,
    val remainingProtein: Double = 145.0,
    val carbsConsumed: Double = 0.0,
    val carbsGoal: Int = 220,
    val remainingCarbs: Double = 220.0,
    val fatConsumed: Double = 0.0,
    val fatGoal: Int = 65,
    val remainingFat: Double = 65.0,
    val stepCount: Int = 0,
    val stepGoal: Int = 10000,
    val distanceKm: Double = 0.0,
    val waterMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val syncSource: String = "Health Connect",
    val isHardwareStepSensor: Boolean = false
)

class NutriStepsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NutriStepsDatabase.getInstance(application)
    private val stepManager = StepTrackingManager(application)
    private val geminiClient = GeminiClient()
    val repository = NutritionRepository(
        foodLogDao = database.foodLogDao(),
        dailyStatsDao = database.dailyStatsDao(),
        userPreferencesDao = database.userPreferencesDao(),
        geminiClient = geminiClient,
        stepTrackingManager = stepManager
    )

    private val _selectedDate = MutableStateFlow(NutritionRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferencesEntity())
    val userPreferences: StateFlow<UserPreferencesEntity> = _userPreferences.asStateFlow()

    private val _todayFoods = MutableStateFlow<List<FoodLogEntity>>(emptyList())
    val todayFoods: StateFlow<List<FoodLogEntity>> = _todayFoods.asStateFlow()

    private val _todayStats = MutableStateFlow(DailyStatsEntity(dateString = NutritionRepository.getTodayDateString()))
    val todayStats: StateFlow<DailyStatsEntity> = _todayStats.asStateFlow()

    private val _recentStats = MutableStateFlow<List<DailyStatsEntity>>(emptyList())
    val recentStats: StateFlow<List<DailyStatsEntity>> = _recentStats.asStateFlow()

    private val _macroSummaries = MutableStateFlow<List<DailyMacroSummary>>(emptyList())
    val macroSummaries: StateFlow<List<DailyMacroSummary>> = _macroSummaries.asStateFlow()

    // Voice Log State
    private val _voiceTranscript = MutableStateFlow("")
    val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    private val _isAnalyzingVoice = MutableStateFlow(false)
    val isAnalyzingVoice: StateFlow<Boolean> = _isAnalyzingVoice.asStateFlow()

    private val _parsedAnalysis = MutableStateFlow<ParsedFoodAnalysis?>(null)
    val parsedAnalysis: StateFlow<ParsedFoodAnalysis?> = _parsedAnalysis.asStateFlow()

    private val _voiceErrorMessage = MutableStateFlow<String?>(null)
    val voiceErrorMessage: StateFlow<String?> = _voiceErrorMessage.asStateFlow()

    // Meal Suggestions State
    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions: StateFlow<Boolean> = _isLoadingSuggestions.asStateFlow()

    private val _suggestedMeals = MutableStateFlow<List<SuggestedMeal>>(emptyList())
    val suggestedMeals: StateFlow<List<SuggestedMeal>> = _suggestedMeals.asStateFlow()

    private val _suggestionMessage = MutableStateFlow<String?>(null)
    val suggestionMessage: StateFlow<String?> = _suggestionMessage.asStateFlow()

    // Sync State
    private val _isSyncingSteps = MutableStateFlow(false)
    val isSyncingSteps: StateFlow<Boolean> = _isSyncingSteps.asStateFlow()

    private val _syncNotification = MutableStateFlow<String?>(null)
    val syncNotification: StateFlow<String?> = _syncNotification.asStateFlow()

    // UI Feedback Banner
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Derived Dashboard Nutrient State
    val nutrientState: StateFlow<DashboardNutrientState> = combine(
        _selectedDate,
        _userPreferences,
        _todayFoods,
        _todayStats
    ) { date, prefs, foods, stats ->
        val consumedCal = foods.sumOf { it.calories }
        val burnedCal = stats.activeCaloriesBurned
        val eatBackFactor = (prefs.eatBackStepCaloriesPercent.coerceIn(0, 100)) / 100.0
        val adjustedBudget = prefs.dailyCalorieTarget + (burnedCal * eatBackFactor)
        val remainingCal = (adjustedBudget - consumedCal)
        val netCal = consumedCal - burnedCal

        val consumedProt = foods.sumOf { it.proteinG }
        val remainingProt = (prefs.dailyProteinTargetG - consumedProt).coerceAtLeast(0.0)

        val consumedCarbs = foods.sumOf { it.carbsG }
        val remainingCarbs = (prefs.dailyCarbsTargetG - consumedCarbs).coerceAtLeast(0.0)

        val consumedFat = foods.sumOf { it.fatG }
        val remainingFat = (prefs.dailyFatTargetG - consumedFat).coerceAtLeast(0.0)

        DashboardNutrientState(
            selectedDate = date,
            baseCalorieGoal = prefs.dailyCalorieTarget,
            consumedCalories = consumedCal,
            burnedCalories = burnedCal,
            adjustedCalorieBudget = adjustedBudget,
            remainingCalories = remainingCal,
            netCalories = netCal,
            proteinConsumed = consumedProt,
            proteinGoal = prefs.dailyProteinTargetG,
            remainingProtein = remainingProt,
            carbsConsumed = consumedCarbs,
            carbsGoal = prefs.dailyCarbsTargetG,
            remainingCarbs = remainingCarbs,
            fatConsumed = consumedFat,
            fatGoal = prefs.dailyFatTargetG,
            remainingFat = remainingFat,
            stepCount = stats.stepCount,
            stepGoal = prefs.dailyStepGoal,
            distanceKm = stats.distanceKm,
            waterMl = stats.waterMl,
            waterGoalMl = 2500,
            syncSource = stats.syncedSource,
            isHardwareStepSensor = repository.isHardwareStepCounterAvailable()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardNutrientState(selectedDate = NutritionRepository.getTodayDateString())
    )

    init {
        repository.startStepSensorListening()
        observeData()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopStepSensorListening()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getUserPreferences().collect { prefs ->
                if (prefs != null) {
                    _userPreferences.value = prefs
                }
            }
        }

        viewModelScope.launch {
            _selectedDate.collect { date ->
                // Collect foods for date
                launch {
                    repository.getFoodsForDate(date).collect { foods ->
                        _todayFoods.value = foods
                    }
                }
                // Collect daily stats for date
                launch {
                    repository.getDailyStats(date).collect { stats ->
                        if (stats != null) {
                            _todayStats.value = stats
                        } else {
                            val defaultStats = DailyStatsEntity(
                                dateString = date,
                                stepCount = 0,
                                activeCaloriesBurned = 0.0,
                                distanceKm = 0.0,
                                waterMl = 0,
                                syncedSource = _userPreferences.value.preferredHealthPlatform
                            )
                            database.dailyStatsDao().upsertDailyStats(defaultStats)
                            _todayStats.value = defaultStats
                        }
                    }
                }
            }
        }

        // Recent stats for charts
        viewModelScope.launch {
            repository.getRecentDaysStats(14).collect { list ->
                _recentStats.value = list
            }
        }

        viewModelScope.launch {
            repository.getDailyMacroTotals(14).collect { list ->
                _macroSummaries.value = list
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun shiftDate(days: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val current = sdf.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = current
                add(Calendar.DAY_OF_YEAR, days)
            }
            _selectedDate.value = sdf.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = NutritionRepository.getTodayDateString()
        }
    }

    // Voice input actions
    fun setVoiceTranscript(text: String) {
        _voiceTranscript.value = text
    }

    fun analyzeVoiceTranscript(mealTypeHint: String = "Lunch") {
        val transcript = _voiceTranscript.value.trim()
        if (transcript.isBlank()) {
            _voiceErrorMessage.value = "Please speak or enter what you ate first."
            return
        }

        viewModelScope.launch {
            _isAnalyzingVoice.value = true
            _voiceErrorMessage.value = null
            try {
                val result = repository.parseVoiceFood(transcript, mealTypeHint)
                _parsedAnalysis.value = result
            } catch (e: Exception) {
                _voiceErrorMessage.value = "Failed to parse: ${e.message}"
            } finally {
                _isAnalyzingVoice.value = false
            }
        }
    }

    fun confirmAndLogParsedAnalysis(mealType: String) {
        val analysis = _parsedAnalysis.value ?: return
        viewModelScope.launch {
            val date = _selectedDate.value
            val entries = analysis.items.map { item ->
                FoodLogEntity(
                    dateString = date,
                    mealType = mealType,
                    foodName = item.name,
                    weightGrams = item.weightGrams,
                    calories = item.calories,
                    proteinG = item.proteinG,
                    carbsG = item.carbsG,
                    fatG = item.fatG,
                    fiberG = item.fiberG,
                    voiceTranscript = _voiceTranscript.value,
                    notes = analysis.summary
                )
            }
            repository.logMultipleFoodItems(entries)
            _parsedAnalysis.value = null
            _voiceTranscript.value = ""
            _userFeedbackMessage.value = "Logged ${entries.size} item(s) to $mealType!"
        }
    }

    fun logDirectFood(
        name: String,
        weightGrams: Double,
        calories: Double,
        proteinG: Double,
        carbsG: Double,
        fatG: Double,
        fiberG: Double = 0.0,
        mealType: String = "Lunch"
    ) {
        viewModelScope.launch {
            val entity = FoodLogEntity(
                dateString = _selectedDate.value,
                mealType = mealType,
                foodName = name,
                weightGrams = weightGrams,
                calories = calories,
                proteinG = proteinG,
                carbsG = carbsG,
                fatG = fatG,
                fiberG = fiberG,
                notes = "Manual entry"
            )
            repository.logFoodItem(entity)
            _userFeedbackMessage.value = "Added $name ($mealType)"
        }
    }

    fun deleteFoodItem(food: FoodLogEntity) {
        viewModelScope.launch {
            repository.deleteFoodItem(food)
            _userFeedbackMessage.value = "Removed ${food.foodName}"
        }
    }

    // AI Meal Suggestions
    fun loadPersonalizedMealSuggestions(mealType: String = "Dinner") {
        viewModelScope.launch {
            _isLoadingSuggestions.value = true
            _suggestionMessage.value = null
            try {
                val state = nutrientState.value
                val remainingCal = state.remainingCalories.coerceAtLeast(150.0)
                val remainingProt = state.remainingProtein.coerceAtLeast(15.0)
                val remainingCarbs = state.remainingCarbs.coerceAtLeast(15.0)
                val remainingFat = state.remainingFat.coerceAtLeast(5.0)

                val suggestions = repository.generateMealSuggestions(
                    remainingCalories = remainingCal,
                    remainingProteinG = remainingProt,
                    remainingCarbsG = remainingCarbs,
                    remainingFatG = remainingFat,
                    activeStepsBurned = state.burnedCalories,
                    mealType = mealType
                )
                _suggestedMeals.value = suggestions
                _suggestionMessage.value = "Generated 3 meals tailored to your remaining targets!"
            } catch (e: Exception) {
                _suggestionMessage.value = "Could not generate suggestions: ${e.message}"
            } finally {
                _isLoadingSuggestions.value = false
            }
        }
    }

    fun logSuggestedMeal(meal: SuggestedMeal) {
        viewModelScope.launch {
            val entity = FoodLogEntity(
                dateString = _selectedDate.value,
                mealType = meal.mealType,
                foodName = meal.title,
                weightGrams = meal.weightGrams,
                calories = meal.calories,
                proteinG = meal.proteinG,
                carbsG = meal.carbsG,
                fatG = meal.fatG,
                notes = meal.healthBenefit
            )
            repository.logFoodItem(entity)
            _userFeedbackMessage.value = "Logged suggested meal: ${meal.title}"
        }
    }

    // Health Platform Sync & Steps
    fun syncHealthPlatform(platformOverride: String? = null) {
        viewModelScope.launch {
            _isSyncingSteps.value = true
            val platform = platformOverride ?: _userPreferences.value.preferredHealthPlatform
            val currentSteps = _todayStats.value.stepCount
            val weight = _userPreferences.value.weightKg
            val date = _selectedDate.value

            repository.syncStepsForDate(
                dateString = date,
                platform = platform,
                currentSteps = currentSteps,
                userWeightKg = weight
            )
            _isSyncingSteps.value = false
            _syncNotification.value = "Synced with $platform successfully!"
        }
    }

    fun addManualSteps(stepDelta: Int) {
        viewModelScope.launch {
            val current = _todayStats.value.stepCount
            val weight = _userPreferences.value.weightKg
            val date = _selectedDate.value

            repository.addManualStepsForDate(
                dateString = date,
                currentSteps = current,
                addedSteps = stepDelta,
                userWeightKg = weight
            )
            _syncNotification.value = if (stepDelta > 0) "+$stepDelta steps logged!" else "$stepDelta steps adjusted"
        }
    }

    fun updateWaterIntake(deltaMl: Int) {
        viewModelScope.launch {
            val current = _todayStats.value.waterMl
            val newAmount = (current + deltaMl).coerceAtLeast(0)
            repository.updateWaterIntake(_selectedDate.value, newAmount)
        }
    }

    fun updatePreferences(
        dailyCalories: Int,
        dailyProtein: Int,
        dailyCarbs: Int,
        dailyFat: Int,
        dailySteps: Int,
        weightKg: Double,
        eatBackPercent: Int,
        platform: String
    ) {
        viewModelScope.launch {
            val updated = _userPreferences.value.copy(
                dailyCalorieTarget = dailyCalories,
                dailyProteinTargetG = dailyProtein,
                dailyCarbsTargetG = dailyCarbs,
                dailyFatTargetG = dailyFat,
                dailyStepGoal = dailySteps,
                weightKg = weightKg,
                eatBackStepCaloriesPercent = eatBackPercent,
                preferredHealthPlatform = platform
            )
            repository.savePreferences(updated)
            _userPreferences.value = updated
            _userFeedbackMessage.value = "Updated daily nutrient & step goals!"
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun clearSyncNotification() {
        _syncNotification.value = null
    }

    fun clearVoiceState() {
        _parsedAnalysis.value = null
        _voiceTranscript.value = ""
        _voiceErrorMessage.value = null
    }
}
