package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // e.g. "Push Day", "Leg Day", "Quick Workout"
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val totalVolumeKg: Double = 0.0,
    val totalSetsCount: Int = 0,
    val phase: String = "Standard", // "Standard", "Comeback", "Deload"
    val isRampUp: Boolean = false
)

@Entity(tableName = "custom_routines")
data class CustomRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val exerciseNamesJson: String = "", // Comma-separated or JSON list of exercise names
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String, // "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    val category: String = "Strength", // "Strength", "Bodyweight", "Cardio"
    val defaultRestSeconds: Int = 90,
    val isCustom: Boolean = false
)

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String = "Other",
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double = 8.0, // Effort rating (1.0 to 10.0)
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val exerciseName: String,
    val maxWeightKg: Double,
    val maxVolumeKg: Double,
    val maxReps: Int,
    val achievedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val weightKg: Double = 0.0,
    val chestCm: Double = 0.0,
    val bicepsCm: Double = 0.0,
    val waistCm: Double = 0.0,
    val thighsCm: Double = 0.0,
    val shouldersCm: Double = 0.0,
    val bodyFatPercentage: Double = 0.0,
    val notes: String = ""
)
