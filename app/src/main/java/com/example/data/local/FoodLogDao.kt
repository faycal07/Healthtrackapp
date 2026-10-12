package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodLogEntity>): List<Long>

    @Update
    suspend fun updateFood(food: FoodLogEntity)

    @Delete
    suspend fun deleteFood(food: FoodLogEntity)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM food_logs WHERE dateString = :date ORDER BY timestamp DESC")
    fun getFoodsForDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFoods(limit: Int = 30): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE dateString IN (:dates) ORDER BY timestamp ASC")
    fun getFoodsForDates(dates: List<String>): Flow<List<FoodLogEntity>>

    @Query("SELECT dateString, SUM(calories) as totalCalories, SUM(proteinG) as totalProtein, SUM(carbsG) as totalCarbs, SUM(fatG) as totalFat FROM food_logs GROUP BY dateString ORDER BY dateString DESC LIMIT :days")
    fun getDailyTotals(days: Int = 14): Flow<List<DailyMacroSummary>>
}

data class DailyMacroSummary(
    val dateString: String,
    val totalCalories: Double?,
    val totalProtein: Double?,
    val totalCarbs: Double?,
    val totalFat: Double?
)
