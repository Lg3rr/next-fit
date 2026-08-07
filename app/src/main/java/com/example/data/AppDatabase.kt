package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WorkoutSessionEntity::class,
        ExerciseEntity::class,
        WorkoutSetEntity::class,
        PersonalRecordEntity::class,
        CustomRoutineEntity::class,
        BodyMeasurementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nextfitness_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun syncDefaultExercises(dao: FitnessDao) {
            val callback = DatabaseCallback(CoroutineScope(Dispatchers.IO))
            callback.syncMissingDefaultExercises(dao)
        }

        class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        INSTANCE?.fitnessDao()?.let { dao ->
                            syncMissingDefaultExercises(dao)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                scope.launch(Dispatchers.IO) {
                    try {
                        INSTANCE?.fitnessDao()?.let { dao ->
                            syncMissingDefaultExercises(dao)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            suspend fun syncMissingDefaultExercises(dao: FitnessDao) {
                try {
                    val existingExercises = dao.getAllExercisesSync()
                    val existingNormalizedNames = existingExercises.map { normalizeName(it.name) }.toSet()

                    val masterExercises = getMasterDefaultExercises()
                    val missingExercises = masterExercises.filter { masterEx ->
                        val normMaster = normalizeName(masterEx.name)
                        !existingNormalizedNames.contains(normMaster) &&
                                existingExercises.none { existing ->
                                    val normExist = normalizeName(existing.name)
                                    normExist == normMaster ||
                                            (normExist.length > 3 && normMaster.length > 3 &&
                                                    (normExist.contains(normMaster) || normMaster.contains(normExist)))
                                }
                    }

                    if (missingExercises.isNotEmpty()) {
                        dao.insertExercises(missingExercises)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            private fun normalizeName(name: String): String {
                return name.lowercase().replace(Regex("[^a-z0-9]"), "")
            }

            private fun getMasterDefaultExercises(): List<ExerciseEntity> {
                return listOf(
                    // --- 1. CHEST ---
                    // Bodyweight
                    ExerciseEntity(name = "Standard Push-Up", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Incline Push-Up", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Decline Push-Up", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Chest Dips", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Diamond Push-Up", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Archer Push-Up", muscleGroup = "Chest", category = "Bodyweight", defaultRestSeconds = 60),
                    // Gym / Equipment
                    ExerciseEntity(name = "Barbell Bench Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Incline Barbell Bench Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Decline Barbell Bench Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Dumbbell Bench Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Incline Dumbbell Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Decline Dumbbell Bench Press", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Dumbbell Flyes", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Incline Dumbbell Flyes", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Cable Crossover", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Cable Chest Fly", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Chest Press Machine", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Pec Deck Machine Flye", muscleGroup = "Chest", category = "Strength", defaultRestSeconds = 60),

                    // --- 2. BACK ---
                    // Bodyweight
                    ExerciseEntity(name = "Pull-Ups", muscleGroup = "Back", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Chin-Up", muscleGroup = "Back", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Inverted Row (Australian Pull-Up)", muscleGroup = "Back", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Superman", muscleGroup = "Back", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Scapular Pull-Up", muscleGroup = "Back", category = "Bodyweight", defaultRestSeconds = 45),
                    // Gym / Equipment
                    ExerciseEntity(name = "Barbell Deadlift", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 180),
                    ExerciseEntity(name = "Barbell Bent Over Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Lat Pulldown", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Single-Arm Dumbbell Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Seated Cable Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "T-Bar Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Chest-Supported Dumbbell Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Chest-Supported Machine Row", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Hyperextension (Back Extension)", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Straight-Arm Cable Pushdown", muscleGroup = "Back", category = "Strength", defaultRestSeconds = 60),

                    // --- 3. SHOULDERS ---
                    // Bodyweight
                    ExerciseEntity(name = "Pike Push-Up", muscleGroup = "Shoulders", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Handstand Push-Up", muscleGroup = "Shoulders", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Bear Crawl Hold", muscleGroup = "Shoulders", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Shoulder Taps", muscleGroup = "Shoulders", category = "Bodyweight", defaultRestSeconds = 45),
                    // Gym / Equipment
                    ExerciseEntity(name = "Overhead Barbell Press", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Seated Dumbbell Shoulder Press", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Dumbbell Lateral Raise", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Cable Lateral Raise", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Rear Delt Dumbbell Flye", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Face Pulls", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Arnold Press", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Barbell Shrugs", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Dumbbell Shrugs", muscleGroup = "Shoulders", category = "Strength", defaultRestSeconds = 60),

                    // --- 4. LEGS ---
                    // Bodyweight
                    ExerciseEntity(name = "Air Squat", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Walking Lunges", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Reverse Lunges", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Bulgarian Split Squat", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Pistol Squat", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Bodyweight Glute Bridge", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Single-Leg Glute Bridge", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Nordic Hamstring Curl", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Standing Calf Raise", muscleGroup = "Legs", category = "Bodyweight", defaultRestSeconds = 45),
                    // Gym / Equipment
                    ExerciseEntity(name = "Barbell Squat", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 180),
                    ExerciseEntity(name = "Barbell Front Squat", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 150),
                    ExerciseEntity(name = "Leg Press", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Romanian Deadlift", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Barbell Hip Thrust", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 120),
                    ExerciseEntity(name = "Leg Extension Machine", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Lying Leg Curl Machine", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Seated Leg Curl Machine", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Goblet Squat", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Seated Machine Calf Raise", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Standing Machine Calf Raise", muscleGroup = "Legs", category = "Strength", defaultRestSeconds = 60),

                    // --- 5. ARMS ---
                    // Bodyweight
                    ExerciseEntity(name = "Triceps Dips", muscleGroup = "Arms", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Close-Grip Push-Up", muscleGroup = "Arms", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Bodyweight Triceps Extension", muscleGroup = "Arms", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Chin-Up Hold", muscleGroup = "Arms", category = "Bodyweight", defaultRestSeconds = 60),
                    // Gym / Equipment
                    ExerciseEntity(name = "Barbell Bicep Curl", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Dumbbell Alternating Curl", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Dumbbell Hammer Curl", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Preacher Curl", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Incline Dumbbell Curl", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Triceps Rope Pushdown", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Skullcrushers (Lying Triceps Extension)", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 90),
                    ExerciseEntity(name = "Overhead Dumbbell Triceps Extension", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Overhead Cable Triceps Extension", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Close-Grip Barbell Bench Press", muscleGroup = "Arms", category = "Strength", defaultRestSeconds = 90),

                    // --- 6. CORE ---
                    // Bodyweight
                    ExerciseEntity(name = "Forearm Plank", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "High Plank", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Hanging Leg Raise", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Hanging Knee Raise", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Abdominal Crunch", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Bicycle Crunch", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Side Plank", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Lying Leg Raise", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Flutter Kicks", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    ExerciseEntity(name = "Hollow Body Hold", muscleGroup = "Core", category = "Bodyweight", defaultRestSeconds = 45),
                    // Gym / Equipment
                    ExerciseEntity(name = "Ab Wheel Rollout", muscleGroup = "Core", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Cable Woodchopper", muscleGroup = "Core", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Weighted Decline Crunch", muscleGroup = "Core", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Hanging Weighted Leg Raise", muscleGroup = "Core", category = "Strength", defaultRestSeconds = 60),
                    ExerciseEntity(name = "Kneeling Cable Crunch", muscleGroup = "Core", category = "Strength", defaultRestSeconds = 60)
                )
            }
        }
    }
}
