package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val dateString: String, // YYYY-MM-DD
    val stepCount: Int = 0,
    val activeCaloriesBurned: Double = 0.0,
    val distanceKm: Double = 0.0,
    val waterMl: Int = 0,
    val syncedSource: String = "Internal Sensor", // "Internal Sensor", "Google Fit", "Health Connect", "Manual"
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
