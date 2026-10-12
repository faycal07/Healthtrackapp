package com.example.data.repository

import com.example.data.health.StepTrackingManager
import com.example.data.local.DailyMacroSummary
import com.example.data.local.DailyStatsDao
import com.example.data.local.DailyStatsEntity
import com.example.data.local.FoodLogDao
import com.example.data.local.FoodLogEntity
import com.example.data.local.UserPreferencesDao
import com.example.data.local.UserPreferencesEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.ParsedFoodAnalysis
import com.example.data.remote.SuggestedMeal
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NutritionRepository(
    private val foodLogDao: FoodLogDao,
    private val dailyStatsDao: DailyStatsDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val geminiClient: GeminiClient,
    private val stepTrackingManager: StepTrackingManager
) {

    fun getFoodsForDate(dateString: String): Flow<List<FoodLogEntity>> =
        foodLogDao.getFoodsForDate(dateString)

    fun getDailyStats(dateString: String): Flow<DailyStatsEntity?> =
        dailyStatsDao.getStatsForDate(dateString)

    fun getUserPreferences(): Flow<UserPreferencesEntity?> =
        userPreferencesDao.getPreferences()

    suspend fun savePreferences(preferences: UserPreferencesEntity) =
        userPreferencesDao.insertOrUpdate(preferences)

    suspend fun logFoodItem(food: FoodLogEntity): Long =
        foodLogDao.insertFood(food)

    suspend fun logMultipleFoodItems(foods: List<FoodLogEntity>): List<Long> =
        foodLogDao.insertAll(foods)

    suspend fun deleteFoodItem(food: FoodLogEntity) =
        foodLogDao.deleteFood(food)

    suspend fun deleteFoodById(id: Long) =
        foodLogDao.deleteById(id)

    suspend fun parseVoiceFood(transcript: String, mealTypeHint: String = "Lunch"): ParsedFoodAnalysis =
        geminiClient.parseFoodFromVoice(transcript, mealTypeHint)

    suspend fun generateMealSuggestions(
        remainingCalories: Double,
        remainingProteinG: Double,
        remainingCarbsG: Double,
        remainingFatG: Double,
        activeStepsBurned: Double,
        mealType: String = "Dinner"
    ): List<SuggestedMeal> =
        geminiClient.generatePersonalizedMealSuggestions(
            remainingCalories = remainingCalories,
            remainingProteinG = remainingProteinG,
            remainingCarbsG = remainingCarbsG,
            remainingFatG = remainingFatG,
            activeStepsBurned = activeStepsBurned,
            mealType = mealType
        )

    suspend fun syncStepsForDate(
        dateString: String,
        platform: String,
        currentSteps: Int,
        userWeightKg: Double
    ) {
        val syncResult = stepTrackingManager.performHealthPlatformSync(
            platformName = platform,
            currentStoredSteps = currentSteps,
            userWeightKg = userWeightKg
        )
        dailyStatsDao.updateSteps(
            date = dateString,
            steps = syncResult.steps,
            burned = syncResult.caloriesBurned,
            distance = syncResult.distanceKm,
            source = syncResult.source,
            timestamp = syncResult.lastSyncTime
        )
    }

    suspend fun addManualStepsForDate(
        dateString: String,
        currentSteps: Int,
        addedSteps: Int,
        userWeightKg: Double
    ) {
        val result = stepTrackingManager.addManualSteps(
            currentSteps = currentSteps,
            addedSteps = addedSteps,
            userWeightKg = userWeightKg
        )
        dailyStatsDao.updateSteps(
            date = dateString,
            steps = result.steps,
            burned = result.caloriesBurned,
            distance = result.distanceKm,
            source = result.source,
            timestamp = result.lastSyncTime
        )
    }

    suspend fun updateWaterIntake(dateString: String, waterMl: Int) {
        dailyStatsDao.updateWater(dateString, waterMl)
    }

    fun getRecentDaysStats(days: Int = 14): Flow<List<DailyStatsEntity>> =
        dailyStatsDao.getRecentDaysStats(days)

    fun getDailyMacroTotals(days: Int = 14): Flow<List<DailyMacroSummary>> =
        foodLogDao.getDailyTotals(days)

    fun isHardwareStepCounterAvailable(): Boolean =
        stepTrackingManager.isHardwarePedometerAvailable

    fun startStepSensorListening() = stepTrackingManager.startListening()
    fun stopStepSensorListening() = stepTrackingManager.stopListening()

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
