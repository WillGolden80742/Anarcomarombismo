package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyExercises(context: Context) {
    private val context = context
    private val cacheManager = Cache()
    private val dateInputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateStorageFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    data class ExerciseByDate(val date: String, val exercise: Exercise, val count: Int = 0)
    private fun getCurrentFormattedDate(): String = dateStorageFormat.format(Date())

    fun markSetsAsDone(date: String = getCurrentFormattedDate(), exercise: Exercise, count: Int = 1) {
        val parsedDate = dateInputFormat.parse(date)
        val formattedDate = dateStorageFormat.format(parsedDate)
        val exerciseHistory = getExerciseHistory(exercise).toMutableSet()

        val existingRecord = exerciseHistory.find {
            it.date == formattedDate &&
                    it.exercise.exerciseID == exercise.exerciseID &&
                    it.exercise.trainingID == exercise.trainingID
        }

        try {
            val updatedRecord = ExerciseByDate(existingRecord!!.date, existingRecord.exercise, existingRecord.count + count)
            exerciseHistory.remove(existingRecord)
            exerciseHistory.add(updatedRecord)
        } catch (e: Exception) {
            exerciseHistory.add(ExerciseByDate(formattedDate, exercise, count))
        }

        updateExerciseCache(exercise, exerciseHistory.toList())
    }

    fun unmarkExercise(date: String = getCurrentFormattedDate(), exercise: Exercise, days: Int = 1) {
        val parsedStartDate = dateInputFormat.parse(date) ?: return
        val exerciseHistory = getExerciseHistory(exercise).toMutableSet()
        val calendar = Calendar.getInstance().apply { time = parsedStartDate }

        repeat(days) {
            val targetDate = dateStorageFormat.format(calendar.time)
            exerciseHistory.removeIf {
                it.date == targetDate &&
                        it.exercise.exerciseID == exercise.exerciseID &&
                        it.exercise.trainingID == exercise.trainingID
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        updateExerciseCache(exercise, exerciseHistory.toList())
    }

    fun isExerciseDone(date: String = getCurrentFormattedDate(), exercise: Exercise, days: Int = 1): Boolean {
        val parsedStartDate = dateInputFormat.parse(date) ?: return false
        val calendar = Calendar.getInstance().apply { time = parsedStartDate }

        repeat(days) {
            val targetDate = dateStorageFormat.format(calendar.time)
            if (getExerciseHistory(exercise).any {
                    it.date == targetDate &&
                            it.exercise.exerciseID == exercise.exerciseID &&
                            it.exercise.trainingID == exercise.trainingID
                }) {
                return true
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return false
    }

    fun getTrainingCompletionPercentage(trainingID: Long): String {
        // Get all exercises for the training
        val exercises = Exercise.build(trainingID).fetchAll(context)
        if (exercises.isEmpty()) return "0"

        // Get current date
        val currentDate = getCurrentFormattedDate()

        // Count completed exercises
        var completedSets = 0
        var totalSets = 0

        exercises.forEach { exercise ->
            val exerciseHistory = getExerciseHistory(exercise)
            val todaysExercise = exerciseHistory.find {
                it.date == currentDate &&
                        it.exercise.exerciseID == exercise.exerciseID &&
                        it.exercise.trainingID == exercise.trainingID
            }

            // Add the completed sets for this exercise
            completedSets += todaysExercise?.count ?: 0
            // Add the total sets required for this exercise
            totalSets += exercise.sets
        }

        // Calculate percentage
        val percentage = if (totalSets > 0) {
            ((completedSets.toDouble() / totalSets.toDouble()) * 100).toInt()
        } else {
            0
        }

        return percentage.toString()
    }

    fun getDaysSinceLastExercise(exercise: Exercise): Int {
        val exerciseHistory = getExerciseHistory(exercise)
        if (exerciseHistory.isEmpty()) return -1

        val lastExerciseDate = exerciseHistory.maxOfOrNull {
            dateStorageFormat.parse(it.date)
        } ?: return -1

        val currentDate = dateStorageFormat.parse(getCurrentFormattedDate())
        val differenceInMillis = currentDate?.time?.minus(lastExerciseDate.time) ?: 0
        val differenceInDays = (differenceInMillis / (1000 * 60 * 60 * 24)).toInt()

        return differenceInDays
    }

    fun getExerciseCount(exercise: Exercise): Int {
        val exerciseHistory= getExerciseHistory(exercise)
        if (exerciseHistory.isEmpty()) return 0

        val lastExercise= exerciseHistory.maxByOrNull {
            dateStorageFormat.parse(it.date)?.time ?: 0
        }

        return lastExercise?.count ?: 0
    }

    fun getExerciseHistory(exercise: Exercise): Set<ExerciseByDate> {
        val cacheKey = "${exercise.exerciseID}-${exercise.trainingID}-exerciseHistory"
        return if (cacheManager.hasCache(context, cacheKey)) {
            cacheManager.getCache(context, cacheKey, Array<ExerciseByDate>::class.java).toSet()
        } else {
            emptySet()
        }
    }

    private fun updateExerciseCache(exercise: Exercise, exerciseHistory: List<ExerciseByDate>) {
        val cacheKey = "${exercise.exerciseID}-${exercise.trainingID}-exerciseHistory"
        cacheManager.setCache(context, cacheKey, exerciseHistory)
    }
}
