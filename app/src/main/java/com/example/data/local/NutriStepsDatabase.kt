package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        FoodLogEntity::class,
        DailyStatsEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutriStepsDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: NutriStepsDatabase? = null

        fun getInstance(context: Context): NutriStepsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriStepsDatabase::class.java,
                    "nutristeps.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate starter preferences and initial sample entries
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.userPreferencesDao().insertOrUpdate(
                                    UserPreferencesEntity(
                                        id = 1,
                                        userName = "Alex",
                                        dailyCalorieTarget = 2200,
                                        dailyProteinTargetG = 145,
                                        dailyCarbsTargetG = 220,
                                        dailyFatTargetG = 65,
                                        dailyStepGoal = 10000,
                                        weightKg = 72.0,
                                        heightCm = 175.0,
                                        activeCalorieBurnRatePerStep = 0.042,
                                        eatBackStepCaloriesPercent = 100,
                                        preferredHealthPlatform = "Health Connect",
                                        isAutoSyncEnabled = true
                                    )
                                )

                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val today = sdf.format(Date())

                                // Initial step stats for today
                                database.dailyStatsDao().upsertDailyStats(
                                    DailyStatsEntity(
                                        dateString = today,
                                        stepCount = 6420,
                                        activeCaloriesBurned = 270.0,
                                        distanceKm = 4.8,
                                        waterMl = 1500,
                                        syncedSource = "Health Connect",
                                        lastSyncTimestamp = System.currentTimeMillis()
                                    )
                                )

                                // Initial starter breakfast
                                database.foodLogDao().insertFood(
                                    FoodLogEntity(
                                        dateString = today,
                                        mealType = "Breakfast",
                                        foodName = "Greek Yogurt Parfait with Blueberries & Honey",
                                        weightGrams = 240.0,
                                        calories = 320.0,
                                        proteinG = 22.0,
                                        carbsG = 38.0,
                                        fatG = 4.5,
                                        fiberG = 3.5,
                                        voiceTranscript = "Logged with NutriSteps AI",
                                        notes = "High-protein breakfast"
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
