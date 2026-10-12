package com.example.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyMacroSummary
import com.example.data.local.DailyStatsEntity
import com.example.ui.theme.CaloriesColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.StepsColor
import java.util.Locale

@Composable
fun HistoryScreen(
    recentStats: List<DailyStatsEntity>,
    macroSummaries: List<DailyMacroSummary>,
    calorieGoal: Int,
    stepGoal: Int,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Merge stats with macro summaries by date
    val mergedDays = remember(recentStats, macroSummaries) {
        val dateSet = (recentStats.map { it.dateString } + macroSummaries.map { it.dateString }).toSortedSet().reversed()
        dateSet.take(14).map { date ->
            val stat = recentStats.find { it.dateString == date }
            val macro = macroSummaries.find { it.dateString == date }
            DayProgressData(
                dateString = date,
                caloriesConsumed = macro?.totalCalories ?: 0.0,
                caloriesBurned = stat?.activeCaloriesBurned ?: 0.0,
                steps = stat?.stepCount ?: 0,
                protein = macro?.totalProtein ?: 0.0,
                carbs = macro?.totalCarbs ?: 0.0,
                fat = macro?.totalFat ?: 0.0
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Progress Over Time",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Calorie balance, steps & macronutrient trends",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key summary metrics
                    val totalSteps = mergedDays.sumOf { it.steps }
                    val totalBurned = mergedDays.sumOf { it.caloriesBurned }
                    val avgConsumed = if (mergedDays.isNotEmpty()) mergedDays.sumOf { it.caloriesConsumed } / mergedDays.size else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryColumn(
                            title = "Total Steps",
                            value = String.format(Locale.US, "%,d", totalSteps),
                            color = StepsColor
                        )
                        SummaryColumn(
                            title = "Active Burn",
                            value = "${totalBurned.toInt()} kcal",
                            color = CoralSecondary
                        )
                        SummaryColumn(
                            title = "Avg Intake",
                            value = "${avgConsumed.toInt()} kcal",
                            color = CaloriesColor
                        )
                    }
                }
            }
        }

        // 2. Calorie Trends Bar Chart (Consumed vs Burned)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Daily Calorie Balance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Food Consumed vs Steps Burned (Goal: $calorieGoal kcal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CalorieTrendsChart(
                        days = mergedDays.reversed().takeLast(7),
                        calorieGoal = calorieGoal.toFloat()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LegendItem(color = CaloriesColor, label = "Food Consumed")
                        Spacer(modifier = Modifier.width(16.dp))
                        LegendItem(color = CoralSecondary, label = "Burned from Steps")
                    }
                }
            }
        }

        // 3. Daily Steps Trend Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Step Count History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily walking & activity (Goal: ${String.format(Locale.US, "%,d", stepGoal)} steps)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StepTrendsChart(
                        days = mergedDays.reversed().takeLast(7),
                        stepGoal = stepGoal
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LegendItem(color = StepsColor, label = "Steps Tracked")
                    }
                }
            }
        }

        // 4. Macro Averages Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Macronutrient Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val avgProtein = if (mergedDays.isNotEmpty()) mergedDays.sumOf { it.protein } / mergedDays.size else 0.0
                    val avgCarbs = if (mergedDays.isNotEmpty()) mergedDays.sumOf { it.carbs } / mergedDays.size else 0.0
                    val avgFat = if (mergedDays.isNotEmpty()) mergedDays.sumOf { it.fat } / mergedDays.size else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MacroAvgPill("Avg Protein", "${avgProtein.toInt()}g", ProteinColor)
                        MacroAvgPill("Avg Carbs", "${avgCarbs.toInt()}g", CarbsColor)
                        MacroAvgPill("Avg Fat", "${avgFat.toInt()}g", FatColor)
                    }
                }
            }
        }

        // 5. Day-by-Day Log List
        item {
            Text(
                text = "Day-by-Day Activity & Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(mergedDays, key = { it.dateString }) { day ->
            DaySummaryCard(
                data = day,
                calorieGoal = calorieGoal,
                onSelect = { onSelectDate(day.dateString) }
            )
        }
    }
}

data class DayProgressData(
    val dateString: String,
    val caloriesConsumed: Double,
    val caloriesBurned: Double,
    val steps: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Composable
fun SummaryColumn(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacroAvgPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CalorieTrendsChart(
    days: List<DayProgressData>,
    calorieGoal: Float
) {
    val maxCalorie = remember(days, calorieGoal) {
        (days.maxOfOrNull { maxOf(it.caloriesConsumed, it.caloriesBurned) }?.toFloat() ?: 2000f).coerceAtLeast(calorieGoal * 1.2f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val chartBottom = height - 30f
            val chartTop = 10f
            val availableHeight = chartBottom - chartTop

            // Draw Goal Reference Line
            val goalY = chartBottom - (calorieGoal / maxCalorie) * availableHeight
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            if (days.isNotEmpty()) {
                val groupWidth = width / days.size
                val barWidth = (groupWidth * 0.3f).coerceAtMost(24f)

                days.forEachIndexed { index, day ->
                    val centerX = groupWidth * index + groupWidth / 2f

                    // Consumed bar
                    val consumedHeight = ((day.caloriesConsumed.toFloat() / maxCalorie) * availableHeight).coerceAtLeast(4f)
                    val consumedTop = chartBottom - consumedHeight
                    drawRoundRect(
                        color = CaloriesColor,
                        topLeft = Offset(centerX - barWidth - 2f, consumedTop),
                        size = Size(barWidth, consumedHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Burned bar
                    val burnedHeight = ((day.caloriesBurned.toFloat() / maxCalorie) * availableHeight).coerceAtLeast(4f)
                    val burnedTop = chartBottom - burnedHeight
                    drawRoundRect(
                        color = CoralSecondary,
                        topLeft = Offset(centerX + 2f, burnedTop),
                        size = Size(barWidth, burnedHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }

        // X-axis date labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                val shortDate = day.dateString.takeLast(5) // MM-DD
                Text(
                    text = shortDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StepTrendsChart(
    days: List<DayProgressData>,
    stepGoal: Int
) {
    val maxSteps = remember(days, stepGoal) {
        (days.maxOfOrNull { it.steps.toFloat() } ?: 10000f).coerceAtLeast(stepGoal.toFloat() * 1.2f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val chartBottom = height - 30f
            val chartTop = 10f
            val availableHeight = chartBottom - chartTop

            // Goal reference line
            val goalY = chartBottom - (stepGoal / maxSteps) * availableHeight
            drawLine(
                color = StepsColor.copy(alpha = 0.5f),
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            if (days.isNotEmpty()) {
                val groupWidth = width / days.size
                val barWidth = (groupWidth * 0.5f).coerceAtMost(36f)

                days.forEachIndexed { index, day ->
                    val centerX = groupWidth * index + groupWidth / 2f
                    val stepHeight = ((day.steps.toFloat() / maxSteps) * availableHeight).coerceAtLeast(4f)
                    val barTop = chartBottom - stepHeight

                    drawRoundRect(
                        color = if (day.steps >= stepGoal) StepsColor else StepsColor.copy(alpha = 0.6f),
                        topLeft = Offset(centerX - barWidth / 2f, barTop),
                        size = Size(barWidth, stepHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }

        // X-axis date labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                val shortDate = day.dateString.takeLast(5)
                Text(
                    text = shortDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DaySummaryCard(
    data: DayProgressData,
    calorieGoal: Int,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("day_summary_${data.dateString}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = data.dateString,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${data.steps} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = StepsColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${data.caloriesBurned.toInt()} kcal burned",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${data.caloriesConsumed.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CaloriesColor
                )
                Text(
                    text = "P: ${data.protein.toInt()}g | C: ${data.carbs.toInt()}g | F: ${data.fat.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
