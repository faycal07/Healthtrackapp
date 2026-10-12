package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FoodLogEntity
import com.example.ui.DashboardNutrientState
import com.example.ui.theme.CaloriesColor
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.CoralSecondary
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SkyContainer
import com.example.ui.theme.SkyTertiary
import com.example.ui.theme.StepsColor
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DashboardNutrientState,
    todayFoods: List<FoodLogEntity>,
    onShiftDate: (Int) -> Unit,
    onResetDateToToday: () -> Unit,
    onOpenVoiceSheet: () -> Unit,
    onOpenManualAdd: () -> Unit,
    onOpenSettings: () -> Unit,
    onSyncSteps: () -> Unit,
    onAddSteps: (Int) -> Unit,
    onUpdateWater: (Int) -> Unit,
    onDeleteFood: (FoodLogEntity) -> Unit,
    onOpenSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Date Selector Header
        item {
            DateSelectorBar(
                selectedDate = state.selectedDate,
                onShiftDate = onShiftDate,
                onResetDateToToday = onResetDateToToday,
                onOpenSettings = onOpenSettings
            )
        }

        // 2. Hero Energy & Calorie Balance Card (Taking steps into account!)
        item {
            CalorieEnergyBalanceCard(
                state = state,
                onOpenVoice = onOpenVoiceSheet
            )
        }

        // 3. Macronutrient Split Cards
        item {
            MacroNutrientsRow(state = state)
        }

        // 4. Live Steps & Health Platform Card
        item {
            StepsActivityCard(
                state = state,
                onSyncSteps = onSyncSteps,
                onAddSteps = onAddSteps
            )
        }

        // 5. Hydration Quick Tracker
        item {
            WaterTrackerCard(
                waterMl = state.waterMl,
                goalMl = state.waterGoalMl,
                onUpdateWater = onUpdateWater
            )
        }

        // 6. AI Meal Suggestions Promo Card
        item {
            AiSuggestionPromoCard(
                remainingCalories = state.remainingCalories,
                remainingProtein = state.remainingProtein,
                onOpenSuggestions = onOpenSuggestions
            )
        }

        // 7. Today's Logged Meals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Meals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${todayFoods.size} items logged (${state.consumedCalories.toInt()} kcal)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onOpenManualAdd,
                        modifier = Modifier.testTag("manual_add_food_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Manual Add",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Manual")
                    }

                    Button(
                        onClick = onOpenVoiceSheet,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.testTag("voice_log_fab_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Log",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Log")
                    }
                }
            }
        }

        if (todayFoods.isEmpty()) {
            item {
                EmptyMealsCard(onOpenVoice = onOpenVoiceSheet)
            }
        } else {
            // Group by meal types
            val mealGroups = todayFoods.groupBy { it.mealType }
            val orderedMealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

            orderedMealTypes.forEach { mealType ->
                val group = mealGroups[mealType]
                if (!group.isNullOrEmpty()) {
                    item {
                        MealGroupHeader(mealType = mealType, foods = group)
                    }
                    items(group, key = { it.id }) { food ->
                        FoodLogItemCard(
                            food = food,
                            onDelete = { onDeleteFood(food) }
                        )
                    }
                }
            }

            // Other meal types if any
            mealGroups.forEach { (mealType, group) ->
                if (mealType !in orderedMealTypes) {
                    item {
                        MealGroupHeader(mealType = mealType, foods = group)
                    }
                    items(group, key = { it.id }) { food ->
                        FoodLogItemCard(
                            food = food,
                            onDelete = { onDeleteFood(food) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelectorBar(
    selectedDate: String,
    onShiftDate: (Int) -> Unit,
    onResetDateToToday: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onShiftDate(-1) },
                modifier = Modifier.testTag("prev_date_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day"
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AssistChip(
                    onClick = onResetDateToToday,
                    label = { Text("Today") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                        labelColor = EmeraldPrimary
                    ),
                    modifier = Modifier.testTag("reset_today_chip")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onShiftDate(1) },
                    modifier = Modifier.testTag("next_date_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Day"
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("open_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        }
    }
}

@Composable
fun CalorieEnergyBalanceCard(
    state: DashboardNutrientState,
    onOpenVoice: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calorie_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ENERGY BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Daily Calorie Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (state.remainingCalories >= 0) EmeraldPrimary.copy(alpha = 0.15f) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (state.remainingCalories >= 0) "${state.remainingCalories.toInt()} kcal left" else "${(-state.remainingCalories).toInt()} kcal over",
                        color = if (state.remainingCalories >= 0) EmeraldPrimary else Color(0xFFD32F2F),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three pillar metrics: Consumed | Burned (Steps) | Active Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Consumed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.consumedCalories.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CaloriesColor
                    )
                    Text(
                        text = "Food In",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "-",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline
                )

                // Burned from Steps
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = CoralSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${state.burnedCalories.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralSecondary
                        )
                    }
                    Text(
                        text = "Steps Burned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "=",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline
                )

                // Net Calories
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.netCalories.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Net Calories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic equation indicator
            val budgetProgress = if (state.adjustedCalorieBudget > 0) {
                (state.consumedCalories / state.adjustedCalorieBudget).coerceIn(0.0, 1.0).toFloat()
            } else 0f

            val animatedProgress by animateFloatAsState(targetValue = budgetProgress, label = "calorie_progress")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (state.consumedCalories <= state.adjustedCalorieBudget) EmeraldPrimary else Color(0xFFD32F2F),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Base Goal: ${state.baseCalorieGoal} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Adjusted Budget: ${state.adjustedCalorieBudget.toInt()} kcal (+${state.burnedCalories.toInt()} from steps)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

@Composable
fun MacroNutrientsRow(state: DashboardNutrientState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MACRONUTRIENT TARGETS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroPillar(
                    modifier = Modifier.weight(1f),
                    label = "Protein",
                    consumed = state.proteinConsumed,
                    goal = state.proteinGoal,
                    unit = "g",
                    color = ProteinColor
                )

                MacroPillar(
                    modifier = Modifier.weight(1f),
                    label = "Carbs",
                    consumed = state.carbsConsumed,
                    goal = state.carbsGoal,
                    unit = "g",
                    color = CarbsColor
                )

                MacroPillar(
                    modifier = Modifier.weight(1f),
                    label = "Fats",
                    consumed = state.fatConsumed,
                    goal = state.fatGoal,
                    unit = "g",
                    color = FatColor
                )
            }
        }
    }
}

@Composable
fun MacroPillar(
    modifier: Modifier = Modifier,
    label: String,
    consumed: Double,
    goal: Int,
    unit: String,
    color: Color
) {
    val progress = if (goal > 0) (consumed / goal).coerceIn(0.0, 1.0).toFloat() else 0f
    val animatedProg by animateFloatAsState(targetValue = progress, label = "macro_prog_$label")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${consumed.toInt()} / $goal$unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            val remaining = (goal - consumed).coerceAtLeast(0.0).toInt()
            Text(
                text = "$remaining$unit left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProg },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepsActivityCard(
    state: DashboardNutrientState,
    onSyncSteps: () -> Unit,
    onAddSteps: (Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("steps_activity_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(StepsColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = StepsColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Daily Steps & Health Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Platform: ${state.syncSource}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.isHardwareStepSensor) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "LIVE SENSOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSyncSteps,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sync_steps_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main step count readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = String.format(Locale.US, "%,d", state.stepCount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = StepsColor
                    )
                    Text(
                        text = "Goal: ${String.format(Locale.US, "%,d", state.stepGoal)} steps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Burned",
                            tint = CoralSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "+${state.burnedCalories.toInt()} kcal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralSecondary
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.2f", state.distanceKm)} km walked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val stepProgress = if (state.stepGoal > 0) {
                (state.stepCount.toFloat() / state.stepGoal).coerceIn(0f, 1f)
            } else 0f

            LinearProgressIndicator(
                progress = { stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = StepsColor,
                trackColor = StepsColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick step adjustment buttons (e.g. for walk, run, or emulator simulation)
            Text(
                text = "Quick Step Simulator & Activity Boost",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { onAddSteps(500) },
                    label = { Text("+500 steps") },
                    modifier = Modifier.testTag("add_500_steps_chip")
                )
                AssistChip(
                    onClick = { onAddSteps(2000) },
                    label = { Text("+2k steps (Walk)") },
                    modifier = Modifier.testTag("add_2000_steps_chip")
                )
                AssistChip(
                    onClick = { onAddSteps(5000) },
                    label = { Text("+5k steps (Run)") },
                    modifier = Modifier.testTag("add_5000_steps_chip")
                )
            }
        }
    }
}

@Composable
fun WaterTrackerCard(
    waterMl: Int,
    goalMl: Int,
    onUpdateWater: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SkyContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SkyTertiary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water",
                        tint = SkyTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Hydration Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$waterMl / $goalMl ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { onUpdateWater(-250) },
                    enabled = waterMl > 0,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("water_minus_btn")
                ) {
                    Text("-250 ml")
                }

                Button(
                    onClick = { onUpdateWater(250) },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyTertiary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("water_plus_btn")
                ) {
                    Text("+250 ml")
                }
            }
        }
    }
}

@Composable
fun AiSuggestionPromoCard(
    remainingCalories: Double,
    remainingProtein: Double,
    onOpenSuggestions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenSuggestions() }
            .testTag("ai_suggestion_promo_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldDark.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gemini AI Meal Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "You have ${remainingCalories.toInt().coerceAtLeast(0)} kcal & ${remainingProtein.toInt().coerceAtLeast(0)}g protein left today. Tap for 3 meals tailored to your goals!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Explore Suggestions",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MealGroupHeader(mealType: String, foods: List<FoodLogEntity>) {
    val totalCal = foods.sumOf { it.calories }.toInt()
    val totalProt = foods.sumOf { it.proteinG }.toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mealType,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "$totalCal kcal • ${totalProt}g P",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FoodLogItemCard(
    food: FoodLogEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_log_item_${food.id}"),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${food.weightGrams.toInt()}g • ${food.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CaloriesColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "P: ${food.proteinG.toInt()}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProteinColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "C: ${food.carbsG.toInt()}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = CarbsColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "F: ${food.fatG.toInt()}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = FatColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (food.notes.isNotBlank() && food.notes != "Manual entry") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_food_${food.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete food",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyMealsCard(onOpenVoice: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No meals logged for this day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Speak into your phone using Gemini voice recognition, or enter food manually.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenVoice,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("empty_state_voice_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log by Voice with Gemini")
            }
        }
    }
}
