package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.notification.WorkoutNotificationHelper
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = FitnessRepository(database.fitnessDao())

    // --- Preferences & Theme ---
    private val sharedPrefs = application.getSharedPreferences("nextfitness_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(sharedPrefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name)
        } catch (_: Exception) {
            ThemeMode.LIGHT
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _inactivityThresholdDays = MutableStateFlow(sharedPrefs.getInt("inactivity_threshold_days", 14))
    val inactivityThresholdDays: StateFlow<Int> = _inactivityThresholdDays.asStateFlow()

    private val _weightUnit = MutableStateFlow(sharedPrefs.getString("weight_unit", "kg") ?: "kg")
    val weightUnit: StateFlow<String> = _weightUnit.asStateFlow()

    // --- Notification Preferences ---
    private val _dailyReminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("daily_reminder_enabled", true))
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled.asStateFlow()

    init {
        if (_dailyReminderEnabled.value) {
            try {
                WorkoutNotificationHelper.scheduleDailyReminder(application)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val dao = database.fitnessDao()
                AppDatabase.syncDefaultExercises(dao)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Rest Timer State ---
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _initialRestSeconds = MutableStateFlow(90)
    val initialRestSeconds: StateFlow<Int> = _initialRestSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // --- Exercise Library State ---
    private val _selectedMuscleGroup = MutableStateFlow("All")
    val selectedMuscleGroup: StateFlow<String> = _selectedMuscleGroup.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val exercises: StateFlow<List<ExerciseEntity>> = combine(
        _selectedMuscleGroup.flatMapLatest { repository.getExercisesByMuscleGroup(it) },
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Custom Routines State ---
    val customRoutines: StateFlow<List<CustomRoutineEntity>> = repository.customRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Workout State ---
    val activeSession: StateFlow<WorkoutSessionEntity?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSets: StateFlow<List<WorkoutSetEntity>> = activeSession.flatMapLatest { session ->
        if (session != null) {
            repository.getSetsForSession(session.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Completed History & PRs ---
    val completedSessions: StateFlow<List<WorkoutSessionEntity>> = repository.completedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecordEntity>> = repository.allPRs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBodyMeasurements: StateFlow<List<BodyMeasurementEntity>> = repository.allBodyMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isWorkoutLoggedToday: StateFlow<Boolean> = completedSessions.map { sessions ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        sessions.any { s ->
            val sessionDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(s.endTime ?: s.startTime))
            sessionDateStr == todayStr
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Inactivity / Welcome Back Protocol ---
    private val _isWelcomeBackDismissed = MutableStateFlow(false)

    val daysSinceLastWorkout: StateFlow<Int?> = completedSessions.map { sessions ->
        val lastSession = sessions.maxByOrNull { it.endTime ?: it.startTime }
        if (lastSession != null) {
            val lastTime = lastSession.endTime ?: lastSession.startTime
            val diffMs = System.currentTimeMillis() - lastTime
            (diffMs / (1000L * 60 * 60 * 24)).toInt()
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isWelcomeBackTriggered: StateFlow<Boolean> = combine(
        daysSinceLastWorkout,
        _inactivityThresholdDays,
        _isWelcomeBackDismissed
    ) { days, threshold, dismissed ->
        if (dismissed || days == null) false
        else days >= threshold
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- PR Celebration Alert ---
    private val _latestPREvent = MutableStateFlow<PersonalRecordEntity?>(null)
    val latestPREvent: StateFlow<PersonalRecordEntity?> = _latestPREvent.asStateFlow()

    // --- Functions ---

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setInactivityThreshold(days: Int) {
        _inactivityThresholdDays.value = days
        sharedPrefs.edit().putInt("inactivity_threshold_days", days).apply()
    }

    fun dismissWelcomeBackBanner() {
        _isWelcomeBackDismissed.value = true
    }

    fun dismissWelcomeBack() {
        _isWelcomeBackDismissed.value = true
    }

    fun setMuscleFilter(group: String) {
        _selectedMuscleGroup.value = group
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleWeightUnit() {
        val newUnit = if (_weightUnit.value == "kg") "lbs" else "kg"
        _weightUnit.value = newUnit
        sharedPrefs.edit().putString("weight_unit", newUnit).apply()
    }

    fun startWorkout(title: String, phase: String = "Standard", isRampUp: Boolean = false) {
        viewModelScope.launch {
            val sessionName = title.ifBlank { "Workout Session" }
            repository.startNewSession(sessionName, phase = phase, isRampUp = isRampUp)
        }
    }

    fun createCustomRoutine(title: String, description: String, exercises: List<String>) {
        viewModelScope.launch {
            repository.createCustomRoutine(title, description, exercises)
        }
    }

    fun deleteCustomRoutine(routine: CustomRoutineEntity) {
        viewModelScope.launch {
            repository.deleteCustomRoutine(routine)
        }
    }

    fun addExerciseToActiveWorkout(exercise: ExerciseEntity) {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            val existingSets = activeSets.value.filter { it.exerciseId == exercise.id }
            val nextSetNumber = existingSets.size + 1
            val lastSetWeight = existingSets.lastOrNull()?.weightKg ?: 60.0
            val lastSetReps = existingSets.lastOrNull()?.reps ?: 10

            repository.addSetToSession(
                sessionId = session.id,
                exercise = exercise,
                setNumber = nextSetNumber,
                weightKg = lastSetWeight,
                reps = lastSetReps
            )
        }
    }

    fun addSetToExerciseInSession(exerciseId: Long, exerciseName: String, muscleGroup: String, defaultRestSec: Int) {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            val existingSets = activeSets.value.filter { it.exerciseId == exerciseId }
            val nextSetNum = existingSets.size + 1
            val lastSet = existingSets.lastOrNull()

            val tempExercise = ExerciseEntity(
                id = exerciseId,
                name = exerciseName,
                muscleGroup = muscleGroup,
                defaultRestSeconds = defaultRestSec
            )

            repository.addSetToSession(
                sessionId = session.id,
                exercise = tempExercise,
                setNumber = nextSetNum,
                weightKg = lastSet?.weightKg ?: 50.0,
                reps = lastSet?.reps ?: 10
            )
        }
    }

    fun toggleSetCompleted(set: WorkoutSetEntity, defaultRestSeconds: Int) {
        viewModelScope.launch {
            val targetState = !set.isCompleted
            val (isNewPR, prRecord) = repository.toggleSetCompletion(set, targetState)

            if (targetState) {
                // Auto trigger rest timer
                startRestTimer(defaultRestSeconds)

                // Trigger PR celebration if applicable
                if (isNewPR && prRecord != null) {
                    _latestPREvent.value = prRecord
                }
            }
        }
    }

    fun dismissPREvent() {
        _latestPREvent.value = null
    }

    fun updateSetValues(set: WorkoutSetEntity, weight: Double, reps: Int, rpe: Double) {
        viewModelScope.launch {
            repository.updateSetValues(set, weight, reps, rpe)
        }
    }

    fun deleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    fun finishCurrentWorkout(notes: String) {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            repository.finishSession(session, activeSets.value, notes)
            stopRestTimer()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            sharedPrefs.edit().putString("last_workout_date", todayStr).apply()
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean, context: Context) {
        _dailyReminderEnabled.value = enabled
        sharedPrefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
        if (enabled) {
            WorkoutNotificationHelper.scheduleDailyReminder(context)
        } else {
            WorkoutNotificationHelper.cancelDailyReminder(context)
        }
    }

    fun triggerTestNotification(context: Context) {
        WorkoutNotificationHelper.showWorkoutReminderNotification(context)
    }

    fun addBodyMeasurement(
        weightKg: Double,
        chestCm: Double = 0.0,
        bicepsCm: Double = 0.0,
        waistCm: Double = 0.0,
        thighsCm: Double = 0.0,
        shouldersCm: Double = 0.0,
        bodyFatPercentage: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addBodyMeasurement(
                weightKg = weightKg,
                chestCm = chestCm,
                bicepsCm = bicepsCm,
                waistCm = waistCm,
                thighsCm = thighsCm,
                shouldersCm = shouldersCm,
                bodyFatPercentage = bodyFatPercentage,
                notes = notes
            )
        }
    }

    fun deleteBodyMeasurement(measurement: BodyMeasurementEntity) {
        viewModelScope.launch {
            repository.deleteBodyMeasurement(measurement)
        }
    }

    fun discardCurrentWorkout() {
        val session = activeSession.value ?: return
        viewModelScope.launch {
            repository.discardSession(session)
            stopRestTimer()
        }
    }

    fun deleteCompletedWorkout(session: WorkoutSessionEntity) {
        viewModelScope.launch {
            repository.deleteCompletedSession(session)
        }
    }

    fun addCustomExercise(name: String, muscleGroup: String, restSeconds: Int) {
        viewModelScope.launch {
            repository.addCustomExercise(name, muscleGroup, restSeconds)
        }
    }

    // --- Precision Rest Timer Logic ---

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _initialRestSeconds.value = seconds
        _restTimerSeconds.value = seconds
        _isTimerRunning.value = true

        timerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _restTimerSeconds.value -= 1
            }
            if (_restTimerSeconds.value <= 0) {
                _isTimerRunning.value = false
                triggerHapticFeedback()
            }
        }
    }

    fun pauseRestTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resumeRestTimer() {
        if (_restTimerSeconds.value > 0) {
            _isTimerRunning.value = true
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_restTimerSeconds.value > 0 && _isTimerRunning.value) {
                    delay(1000L)
                    _restTimerSeconds.value -= 1
                }
                if (_restTimerSeconds.value <= 0) {
                    _isTimerRunning.value = false
                    triggerHapticFeedback()
                }
            }
        }
    }

    fun adjustTimerSeconds(secondsToAdd: Int) {
        val current = _restTimerSeconds.value
        val updated = maxOf(0, current + secondsToAdd)
        _restTimerSeconds.value = updated
        if (updated > 0 && !_isTimerRunning.value) {
            resumeRestTimer()
        }
    }

    fun stopRestTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _restTimerSeconds.value = 0
    }

    // --- Data Import / Export Backup ---

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("appName", "Next Fit")
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        // Custom Routines
        val routinesArray = JSONArray()
        customRoutines.value.forEach { r ->
            val obj = JSONObject()
            obj.put("title", r.title)
            obj.put("description", r.description)
            obj.put("exercises", r.exerciseNamesJson)
            routinesArray.put(obj)
        }
        root.put("customRoutines", routinesArray)

        // Completed Sessions
        val sessionsArray = JSONArray()
        completedSessions.value.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("startTime", s.startTime)
            obj.put("endTime", s.endTime ?: s.startTime)
            obj.put("totalVolumeKg", s.totalVolumeKg)
            obj.put("totalSetsCount", s.totalSetsCount)
            obj.put("notes", s.notes)
            obj.put("phase", s.phase)
            obj.put("isRampUp", s.isRampUp)
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        // PRs
        val prsArray = JSONArray()
        personalRecords.value.forEach { pr ->
            val obj = JSONObject()
            obj.put("exerciseId", pr.exerciseId)
            obj.put("exerciseName", pr.exerciseName)
            obj.put("maxWeightKg", pr.maxWeightKg)
            obj.put("maxVolumeKg", pr.maxVolumeKg)
            obj.put("maxReps", pr.maxReps)
            obj.put("achievedAt", pr.achievedAt)
            prsArray.put(obj)
        }
        root.put("personalRecords", prsArray)

        // Body Measurements
        val bodyArray = JSONArray()
        allBodyMeasurements.value.forEach { bm ->
            val obj = JSONObject()
            obj.put("weightKg", bm.weightKg)
            obj.put("chestCm", bm.chestCm)
            obj.put("bicepsCm", bm.bicepsCm)
            obj.put("waistCm", bm.waistCm)
            obj.put("thighsCm", bm.thighsCm)
            obj.put("shouldersCm", bm.shouldersCm)
            obj.put("bodyFatPercentage", bm.bodyFatPercentage)
            obj.put("notes", bm.notes)
            obj.put("dateTimestamp", bm.dateTimestamp)
            bodyArray.put(obj)
        }
        root.put("bodyMeasurements", bodyArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // Import Custom Routines
            if (root.has("customRoutines")) {
                val array = root.getJSONArray("customRoutines")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val title = obj.optString("title", "Custom Routine")
                    val desc = obj.optString("description", "")
                    val exListStr = obj.optString("exercises", "")
                    repository.createCustomRoutine(title, desc, exListStr.split(",").map { it.trim() }.filter { it.isNotBlank() })
                }
            }

            // Import Sessions
            if (root.has("sessions")) {
                val array = root.getJSONArray("sessions")
                val sessionsToInsert = mutableListOf<WorkoutSessionEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    sessionsToInsert.add(
                        WorkoutSessionEntity(
                            id = 0, // Auto generate new ID
                            title = obj.optString("title", "Imported Session"),
                            startTime = obj.optLong("startTime", System.currentTimeMillis()),
                            endTime = obj.optLong("endTime", System.currentTimeMillis()),
                            isCompleted = true,
                            notes = obj.optString("notes", ""),
                            totalVolumeKg = obj.optDouble("totalVolumeKg", 0.0),
                            totalSetsCount = obj.optInt("totalSetsCount", 0),
                            phase = obj.optString("phase", "Imported"),
                            isRampUp = obj.optBoolean("isRampUp", false)
                        )
                    )
                }
                if (sessionsToInsert.isNotEmpty()) {
                    database.fitnessDao().insertWorkoutSessions(sessionsToInsert)
                }
            }

            // Import PRs
            if (root.has("personalRecords")) {
                val array = root.getJSONArray("personalRecords")
                val prsToInsert = mutableListOf<PersonalRecordEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    prsToInsert.add(
                        PersonalRecordEntity(
                            id = 0,
                            exerciseId = obj.optLong("exerciseId", 0L),
                            exerciseName = obj.optString("exerciseName", "Exercise"),
                            maxWeightKg = obj.optDouble("maxWeightKg", 0.0),
                            maxVolumeKg = obj.optDouble("maxVolumeKg", 0.0),
                            maxReps = obj.optInt("maxReps", 0),
                            achievedAt = obj.optLong("achievedAt", System.currentTimeMillis())
                        )
                    )
                }
                if (prsToInsert.isNotEmpty()) {
                    database.fitnessDao().insertPRs(prsToInsert)
                }
            }

            // Import Body Measurements
            if (root.has("bodyMeasurements")) {
                val array = root.getJSONArray("bodyMeasurements")
                val measurementsToInsert = mutableListOf<BodyMeasurementEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    measurementsToInsert.add(
                        BodyMeasurementEntity(
                            id = 0,
                            weightKg = obj.optDouble("weightKg", 0.0),
                            chestCm = obj.optDouble("chestCm", 0.0),
                            bicepsCm = obj.optDouble("bicepsCm", 0.0),
                            waistCm = obj.optDouble("waistCm", 0.0),
                            thighsCm = obj.optDouble("thighsCm", 0.0),
                            shouldersCm = obj.optDouble("shouldersCm", 0.0),
                            bodyFatPercentage = obj.optDouble("bodyFatPercentage", 0.0),
                            notes = obj.optString("notes", ""),
                            dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (measurementsToInsert.isNotEmpty()) {
                    database.fitnessDao().insertBodyMeasurements(measurementsToInsert)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun triggerHapticFeedback() {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (_: Exception) {
            // Haptics fallback gracefully if vibrator hardware is unavailable
        }
    }
}
