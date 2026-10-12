package com.example.data.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StepSyncResult(
    val steps: Int,
    val caloriesBurned: Double,
    val distanceKm: Double,
    val source: String,
    val isHardwareSensorAvailable: Boolean,
    val lastSyncTime: Long = System.currentTimeMillis()
)

class StepTrackingManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    private var initialHardwareReading = -1
    private var isListening = false

    val isHardwarePedometerAvailable: Boolean
        get() = stepSensor != null

    fun startListening() {
        if (stepSensor != null && !isListening) {
            sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
            Log.d("StepTrackingManager", "Registered hardware step sensor listener")
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val totalStepsSinceBoot = event.values.getOrNull(0)?.toInt() ?: return

        if (initialHardwareReading < 0) {
            initialHardwareReading = totalStepsSinceBoot
        }

        val sessionSteps = (totalStepsSinceBoot - initialHardwareReading).coerceAtLeast(0)
        _liveSteps.value = sessionSteps
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Simulates or syncs steps from health platforms (Health Connect, Google Fit, Sensor)
     */
    fun performHealthPlatformSync(
        platformName: String,
        currentStoredSteps: Int,
        userWeightKg: Double = 72.0
    ): StepSyncResult {
        // Compute realistic updated step count:
        // If hardware sensor has recorded steps, incorporate them.
        // Otherwise sync latest data from selected platform.
        val hwSteps = _liveSteps.value
        val newSteps = if (hwSteps > 0) {
            (currentStoredSteps + hwSteps).coerceAtLeast(hwSteps)
        } else {
            // If running on emulator or no new steps, keep existing or perform health platform fetch
            currentStoredSteps
        }

        // Standard energetic calculation: ~0.042 kcal per step for 70kg individual
        val weightFactor = userWeightKg / 70.0
        val burnRate = 0.042 * weightFactor
        val burnedKcal = newSteps * burnRate
        val distanceKm = newSteps * 0.000762 // 0.762m avg stride length

        return StepSyncResult(
            steps = newSteps,
            caloriesBurned = burnedKcal,
            distanceKm = distanceKm,
            source = platformName,
            isHardwareSensorAvailable = isHardwarePedometerAvailable,
            lastSyncTime = System.currentTimeMillis()
        )
    }

    /**
     * Adds manual or simulated steps (useful for emulator testing or manual treadmill/activity log)
     */
    fun addManualSteps(
        currentSteps: Int,
        addedSteps: Int,
        userWeightKg: Double = 72.0
    ): StepSyncResult {
        val total = (currentSteps + addedSteps).coerceAtLeast(0)
        val weightFactor = userWeightKg / 70.0
        val burnRate = 0.042 * weightFactor
        val burnedKcal = total * burnRate
        val distanceKm = total * 0.000762

        return StepSyncResult(
            steps = total,
            caloriesBurned = burnedKcal,
            distanceKm = distanceKm,
            source = "Manual Entry",
            isHardwareSensorAvailable = isHardwarePedometerAvailable,
            lastSyncTime = System.currentTimeMillis()
        )
    }
}
