package com.example.data

import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {

    val allExercises: Flow<List<ExerciseEntity>> = fitnessDao.getAllExercises()
    val activeSession: Flow<WorkoutSessionEntity?> = fitnessDao.getActiveSession()
    val completedSessions: Flow<List<WorkoutSessionEntity>> = fitnessDao.getCompletedSessions()
    val allPRs: Flow<List<PersonalRecordEntity>> = fitnessDao.getAllPRs()
    val allBodyMeasurements: Flow<List<BodyMeasurementEntity>> = fitnessDao.getAllBodyMeasurements()

    suspend fun addBodyMeasurement(
        weightKg: Double,
        chestCm: Double = 0.0,
        bicepsCm: Double = 0.0,
        waistCm: Double = 0.0,
        thighsCm: Double = 0.0,
        shouldersCm: Double = 0.0,
        bodyFatPercentage: Double = 0.0,
        notes: String = ""
    ): Long {
        val measurement = BodyMeasurementEntity(
            dateTimestamp = System.currentTimeMillis(),
            weightKg = weightKg,
            chestCm = chestCm,
            bicepsCm = bicepsCm,
            waistCm = waistCm,
            thighsCm = thighsCm,
            shouldersCm = shouldersCm,
            bodyFatPercentage = bodyFatPercentage,
            notes = notes
        )
        return fitnessDao.insertBodyMeasurement(measurement)
    }

    suspend fun deleteBodyMeasurement(measurement: BodyMeasurementEntity) {
        fitnessDao.deleteBodyMeasurement(measurement)
    }

    suspend fun getAllCompletedSetsSync(): List<WorkoutSetEntity> {
        return fitnessDao.getAllCompletedSetsSync()
    }

    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> {
        return if (muscleGroup.lowercase() == "all") {
            fitnessDao.getAllExercises()
        } else {
            fitnessDao.getExercisesByMuscleGroup(muscleGroup)
        }
    }

    suspend fun addCustomExercise(name: String, muscleGroup: String, defaultRestSeconds: Int): Long {
        val exercise = ExerciseEntity(
            name = name,
            muscleGroup = muscleGroup,
            category = "Strength",
            defaultRestSeconds = defaultRestSeconds,
            isCustom = true
        )
        return fitnessDao.insertExercise(exercise)
    }

    val customRoutines: Flow<List<CustomRoutineEntity>> = fitnessDao.getAllCustomRoutines()

    suspend fun createCustomRoutine(title: String, description: String, exerciseNames: List<String>): Long {
        val routine = CustomRoutineEntity(
            title = title,
            description = description,
            exerciseNamesJson = exerciseNames.joinToString(",")
        )
        return fitnessDao.insertCustomRoutine(routine)
    }

    suspend fun deleteCustomRoutine(routine: CustomRoutineEntity) {
        fitnessDao.deleteCustomRoutine(routine)
    }

    suspend fun startNewSession(title: String, phase: String = "Standard", isRampUp: Boolean = false): Long {
        val session = WorkoutSessionEntity(
            title = title,
            startTime = System.currentTimeMillis(),
            isCompleted = false,
            phase = phase,
            isRampUp = isRampUp
        )
        return fitnessDao.insertSession(session)
    }

    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>> {
        return fitnessDao.getSetsForSession(sessionId)
    }

    suspend fun addSetToSession(
        sessionId: Long,
        exercise: ExerciseEntity,
        setNumber: Int,
        weightKg: Double,
        reps: Int,
        rpe: Double = 8.0
    ): Long {
        val set = WorkoutSetEntity(
            workoutSessionId = sessionId,
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            muscleGroup = exercise.muscleGroup,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps,
            rpe = rpe,
            isCompleted = false
        )
        return fitnessDao.insertSet(set)
    }

    suspend fun toggleSetCompletion(set: WorkoutSetEntity, isCompleted: Boolean): Pair<Boolean, PersonalRecordEntity?> {
        val updatedSet = set.copy(isCompleted = isCompleted)
        fitnessDao.updateSet(updatedSet)

        var newPR: PersonalRecordEntity? = null
        var isNewPR = false

        if (isCompleted) {
            // Check for PR
            val currentPR = fitnessDao.getPRForExercise(set.exerciseId)
            val volume = set.weightKg * set.reps
            
            val isWeightPR = currentPR == null || set.weightKg > currentPR.maxWeightKg
            val isVolumePR = currentPR == null || volume > currentPR.maxVolumeKg
            val isRepsPR = currentPR == null || set.reps > currentPR.maxReps

            if (isWeightPR || isVolumePR || isRepsPR) {
                isNewPR = true
                val newRecord = PersonalRecordEntity(
                    id = currentPR?.id ?: 0,
                    exerciseId = set.exerciseId,
                    exerciseName = set.exerciseName,
                    maxWeightKg = maxOf(currentPR?.maxWeightKg ?: 0.0, set.weightKg),
                    maxVolumeKg = maxOf(currentPR?.maxVolumeKg ?: 0.0, volume),
                    maxReps = maxOf(currentPR?.maxReps ?: 0, set.reps),
                    achievedAt = System.currentTimeMillis()
                )
                fitnessDao.insertPR(newRecord)
                newPR = newRecord
            }
        }
        return Pair(isNewPR, newPR)
    }

    suspend fun updateSetValues(set: WorkoutSetEntity, weightKg: Double, reps: Int, rpe: Double) {
        val updated = set.copy(weightKg = weightKg, reps = reps, rpe = rpe)
        fitnessDao.updateSet(updated)
    }

    suspend fun deleteSet(set: WorkoutSetEntity) {
        fitnessDao.deleteSet(set)
    }

    suspend fun finishSession(session: WorkoutSessionEntity, sets: List<WorkoutSetEntity>, notes: String = "") {
        val completedSets = sets.filter { it.isCompleted }
        val totalVolume = completedSets.fold(0.0) { acc, set -> acc + (set.weightKg * set.reps) }
        val updatedSession = session.copy(
            endTime = System.currentTimeMillis(),
            isCompleted = true,
            notes = notes,
            totalVolumeKg = totalVolume,
            totalSetsCount = completedSets.size
        )
        fitnessDao.updateSession(updatedSession)
    }

    suspend fun discardSession(session: WorkoutSessionEntity) {
        fitnessDao.deleteSetsForSession(session.id)
        fitnessDao.deleteSession(session)
    }

    suspend fun deleteCompletedSession(session: WorkoutSessionEntity) {
        fitnessDao.deleteSetsForSession(session.id)
        fitnessDao.deleteSession(session)
    }
}
