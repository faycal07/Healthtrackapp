package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyStats(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE dateString = :date LIMIT 1")
    fun getStatsForDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE dateString = :date LIMIT 1")
    suspend fun getStatsForDateOnce(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT :days")
    fun getRecentDaysStats(days: Int = 14): Flow<List<DailyStatsEntity>>

    @Query("UPDATE daily_stats SET stepCount = :steps, activeCaloriesBurned = :burned, distanceKm = :distance, syncedSource = :source, lastSyncTimestamp = :timestamp WHERE dateString = :date")
    suspend fun updateSteps(date: String, steps: Int, burned: Double, distance: Double, source: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE daily_stats SET waterMl = :waterMl WHERE dateString = :date")
    suspend fun updateWater(date: String, waterMl: Int)
}
