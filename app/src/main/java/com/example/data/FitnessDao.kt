package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    // --- Exercises ---
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesSync(): List<ExerciseEntity>

    // --- Workout Sessions ---
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    // --- Workout Sets ---
    @Query("SELECT * FROM workout_sets WHERE workoutSessionId = :sessionId ORDER BY exerciseId ASC, setNumber ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isCompleted = 1 ORDER BY timestamp DESC")
    fun getCompletedSetsForExercise(exerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE workoutSessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    // --- Custom Routines ---
    @Query("SELECT * FROM custom_routines ORDER BY id DESC")
    fun getAllCustomRoutines(): Flow<List<CustomRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRoutine(routine: CustomRoutineEntity): Long

    @Delete
    suspend fun deleteCustomRoutine(routine: CustomRoutineEntity)

    // --- Bulk Data & Backup Queries ---
    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllWorkoutSessions()

    @Query("DELETE FROM workout_sets")
    suspend fun deleteAllWorkoutSets()

    @Query("DELETE FROM personal_records")
    suspend fun deleteAllPRs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSessions(sessions: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPRs(prs: List<PersonalRecordEntity>)

    @Query("SELECT * FROM workout_sets WHERE isCompleted = 1")
    suspend fun getAllCompletedSetsSync(): List<WorkoutSetEntity>

    // --- Personal Records ---
    @Query("SELECT * FROM personal_records ORDER BY exerciseName ASC")
    fun getAllPRs(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getPRForExercise(exerciseId: Long): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPR(pr: PersonalRecordEntity)

    // --- Body Measurements & Weight Logs ---
    @Query("SELECT * FROM body_measurements ORDER BY dateTimestamp DESC")
    fun getAllBodyMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMeasurement(measurement: BodyMeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMeasurements(measurements: List<BodyMeasurementEntity>)

    @Delete
    suspend fun deleteBodyMeasurement(measurement: BodyMeasurementEntity)
}
