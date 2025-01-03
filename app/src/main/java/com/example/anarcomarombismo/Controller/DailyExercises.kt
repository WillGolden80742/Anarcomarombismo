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
        val exercises =  Exercise.build(trainingID).fetchAll(context)
        if (exercises.isEmpty()) return "0"

        val currentDate = getCurrentFormattedDate()
        val (completedSets, totalSets) = calculateSetsCompletion(exercises, currentDate)

        return calculateCompletionPercentage(completedSets, totalSets).toString()
    }

    private fun calculateSetsCompletion(exercises: List<Exercise>, currentDate: String): Pair<Int, Int> {
        var completedSets = 0
        var totalSets = 0

        exercises.forEach { exercise ->
            val exerciseHistory = getExerciseHistory(exercise)
            val todaysExercise = exerciseHistory.find {
                it.date == currentDate &&
                        it.exercise.exerciseID == exercise.exerciseID &&
                        it.exercise.trainingID == exercise.trainingID
            }

            completedSets += todaysExercise?.count ?: 0
            totalSets += exercise.sets
        }
        return Pair(completedSets, totalSets)
    }

    fun getTotalSets(trainingID: Long): Int {
        val exercises = Exercise.build(trainingID).fetchAll(context)
        var totalSets = 0
        exercises.forEach { exercise ->
            totalSets += exercise.sets
        }
        return totalSets
    }
    fun getCompletedSets(trainingID: Long): Int {
        val exercises = Exercise.build(trainingID).fetchAll(context)
        val currentDate = getCurrentFormattedDate()
        var completedSets = 0
        exercises.forEach { exercise ->
            val exerciseHistory = getExerciseHistory(exercise)
            val todaysExercise = exerciseHistory.find {
                it.date == currentDate &&
                        it.exercise.exerciseID == exercise.exerciseID &&
                        it.exercise.trainingID == exercise.trainingID
            }

            completedSets += todaysExercise?.count ?: 0
        }
        return completedSets
    }

    private fun calculateCompletionPercentage(completedSets: Int, totalSets: Int): Int {
        return if (totalSets > 0) {
            ((completedSets.toDouble() / totalSets.toDouble()) * 100).toInt()
        } else {
            0
        }
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

    fun getExerciseStatusText(currentExercise: Exercise):String {
        val countDays = getDaysSinceLastExercise(currentExercise)
        val exerciseCount = getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        val daysText = when {
            countDays > 1 -> "$countDays ${context.getString(R.string.days)}"
            countDays == 1 -> "$countDays ${context.getString(R.string.day)}"
            countDays == 0 -> "$exerciseCount/$sets"
            else -> ""
        }
        return daysText
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
