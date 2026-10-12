package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Health Enthusiast",
    val dailyCalorieTarget: Int = 2100,
    val dailyProteinTargetG: Int = 140,
    val dailyCarbsTargetG: Int = 210,
    val dailyFatTargetG: Int = 65,
    val dailyStepGoal: Int = 10000,
    val weightKg: Double = 72.0,
    val heightCm: Double = 175.0,
    val activeCalorieBurnRatePerStep: Double = 0.042, // kcal per step
    val eatBackStepCaloriesPercent: Int = 100, // 0 to 100%
    val preferredHealthPlatform: String = "Health Connect", // "Health Connect", "Google Fit", "Device Sensor"
    val isAutoSyncEnabled: Boolean = true
)
