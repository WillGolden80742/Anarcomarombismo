package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.R
import java.util.Random

class Exercise(
    var trainingID: Long = 0,
    var LinkVideo: String = "",
    var exerciseID: Long = 0,
    var name: String = "Exercício",
    var muscle: String = "",
    var sets: Int = 3,
    var repetitions: String = "10,10,10",
    var load: Double = 20.0, // Carga padrão em kg
    var rest: Int = 60, // Tempo de repouso padrão em segundos
    var cadence: String = "3-1-3", // Cadência padrão
) {

    private val cache = Cache()
    private val jsonUtil = JSON()
    fun saveExercise(context: Context):Boolean {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = getExerciseArray(context,cacheKey)
        val updatedExerciseArray = updateExerciseArray(exerciseArray, this@Exercise)
        saveExerciseArray(context,cacheKey, updatedExerciseArray)
        showToastMessage(context,exerciseID > 0)
        return true
    }

    private fun saveExerciseArray(context: Context,cacheKey: String, exerciseArray: Array<Exercise>) {
        cache.setCache(context, cacheKey, jsonUtil.toJson(exerciseArray))
    }

    private fun showToastMessage(context: Context,isUpdate: Boolean) {
        val messageResId = if (isUpdate) R.string.update_exercise_successful else R.string.save_exercise_successful
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun updateExerciseArray(
        exerciseArray: Array<Exercise>,
        exercise: Exercise
    ): Array<Exercise> {
        return if (exerciseID > 0) {
            exerciseArray.map { if (it.exerciseID == exerciseID) exercise else it }.toTypedArray()
        } else {
            exercise.exerciseID = exerciseID.takeIf { it > 0 } ?: System.currentTimeMillis() + Random().nextInt(100)
            exerciseArray.plus(exercise)
        }
    }
    private fun getExerciseArray(context: Context,cacheKey: String): Array<Exercise> {
        return if (cache.hasCache(context, cacheKey)) {
            jsonUtil.fromJson(cache.getCache(context, cacheKey), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
    }


    fun generateYouTubeEmbedLink(text: String): String {
        val trimmedText = text.trim()

        if (trimmedText.contains("youtube.com/embed/")) {
            return trimmedText
        }

        if (!isValidYouTubeLink(trimmedText)) {
            return ""
        }

        val sanitizedText = removeUnnecessaryParameters(trimmedText)
        val videoId = extractVideoId(sanitizedText) ?: return ""

        return buildEmbedLink(videoId, sanitizedText)
    }

    private fun isValidYouTubeLink(text: String): Boolean {
        return text.contains("youtu.be") || text.contains("youtube")
    }

    private fun removeUnnecessaryParameters(text: String): String {
        return text.replace(Regex("[&?](feature=youtu\\.be|si=.*)"), "")
    }

    private fun extractVideoId(text: String): String? {
        return when {
            text.contains("/live/") -> text.substringAfter("/live/").substringBefore("?")
            text.contains("/shorts/") -> text.substringAfter("/shorts/").substringBefore("?")
            text.contains("watch?v=") -> text.substringAfter("watch?v=").substringBefore("&")
            text.contains("youtu.be/") -> text.substringAfter("youtu.be/").substringBefore("?")
            else -> null
        }
    }

    private fun buildEmbedLink(videoId: String, text: String): String {
        val timeParameter = extractTimeParameter(text)
        return "https://www.youtube.com/embed/$videoId$timeParameter"
    }

    private fun extractTimeParameter(text: String): String {
        val time = text.substringAfter("&t=", "").takeIf { it.isNotEmpty() }
        return time?.let { "?t=$it" } ?: ""
    }


    fun toString(context: Context): String {
        return "${context.getString(R.string.muscle)}: $muscle, \n${context.getString(R.string.sets)}: $sets,\n${context.getString(R.string.reps)}: $repetitions, \n${context.getString(R.string.load)}: $load, \n${context.getString(R.string.rest)}: $rest, \n${context.getString(R.string.cadence)}: $cadence"
    }

    override fun toString(): String {
        return "Muscle: $muscle, \nsets: $sets, \nrepetitions: $repetitions, \nload:$load, \nrest: $rest, \ncadence: '$cadence'"
    }
}
