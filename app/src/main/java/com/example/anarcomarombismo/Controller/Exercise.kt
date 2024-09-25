package com.example.anarcomarombismo.Controller

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.ContextualExercise
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.util.Random

class Exercise(
    var trainingID: Long = 0,
    var linkVideo: String = "",
    var exerciseID: Long = 0,
    var name: String = "Exercício",
    var muscle: String = "",
    var sets: Int = 3,
    var repetitions: String = "10,10,10",
    var load: Double = 20.0,
    var rest: Int = 60,
    var cadence: String = "3-1-3",
): DataHandler<Exercise> {
    companion object {
        private val cache = Cache()

        fun build(
            trainingID: Long = 0L,
            linkVideo: String = "",
            exerciseID: Long = 0L,
            name: String = "",
            muscle: String = "",
            sets: Int = 0,
            repetitions: String = "1",
            load: Double = 0.0,
            rest: Int = 60,
            cadence: String = "3-1-3"
        ): Exercise {
            return Exercise().apply {
                this.trainingID = trainingID
                this.linkVideo = linkVideo
                this.exerciseID = exerciseID.takeIf { it > 0 } ?: System.currentTimeMillis() + Random().nextInt(100)
                this.name = name
                this.muscle = muscle
                this.sets = sets
                this.repetitions = repetitions
                this.load = load
                this.rest = rest
                this.cadence = cadence
            }
        }

        fun dumpExercise(context: Context) {
            val contextualKey = context.getString(R.string.exercises)
            val exerciseIds = listOf(R.raw.training_1, R.raw.training_2, R.raw.training_3, R.raw.training_4)
            exerciseIds.forEach { exerciseId ->
                val trainingData = context.resources.openRawResource(exerciseId).bufferedReader().use { it.readText() }
                val contextualExerciseList = JSON.fromJson(trainingData, Array<ContextualExercise>::class.java)
                val exerciseList = ContextualExercise.getExercise(context,contextualExerciseList).toList()
                cache.setCache(context, "${contextualKey}_${exerciseList[0].trainingID}", exerciseList)
            }
        }
    }

    override fun save(context: Context): Boolean {
        val contextualKey = context.getString(R.string.exercises)
        val cacheKey = "${contextualKey}_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val updatedExerciseArray = updateExerciseArray(exerciseArray)
        saveExerciseArray(context, cacheKey, updatedExerciseArray)
        showToastMessage(context, exerciseID in exerciseArray.map { it.exerciseID })
        return true
    }

    override fun remove(context: Context): Boolean {
        val contextualKey = context.getString(R.string.exercises)
        val cacheKey = "${contextualKey}_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val newExerciseArray = exerciseArray.filter { it.exerciseID != exerciseID }.toTypedArray()
        saveExerciseArray(context, cacheKey, newExerciseArray)
        showToastMessage(context, false, R.string.remove_exercise_successful, R.string.remove_exercise_successful)
        return true
    }

    override fun fetchById(context: Context, id: Any): Exercise? {
        val contextualKey = context.getString(R.string.exercises)
        val cacheKey = "${contextualKey}_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        return exerciseArray.find { it.exerciseID == id as Long }
    }

    override fun fetchAll(context: Context): List<Exercise> {
        val contextualKey = context.getString(R.string.exercises)
        val cacheKey = "${contextualKey}_$trainingID"
        val exerciseArray = if (cache.hasCache(context, cacheKey)) {
            cache.getCache(context, cacheKey, Array<Exercise>::class.java)
        } else {
            val random = Random().nextInt(100)
            val defaultExerciseArray = arrayOf(
                Exercise(trainingID, "", System.currentTimeMillis() + random, "Exercicio", "", 3, "10,10,10", 0.0)
            )
            cache.setCache(context, cacheKey, defaultExerciseArray)
            defaultExerciseArray
        }

        for (exercise in exerciseArray) {
            println("Exercício em Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
        }

        return exerciseArray.toList()
    }

    private fun getExerciseArray(context: Context, cacheKey: String): Array<Exercise> {
        return if (cache.hasCache(context, cacheKey)) {
            cache.getCache(context, cacheKey,Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
    }

    private fun updateExerciseArray(exerciseArray: Array<Exercise>): Array<Exercise> {
        val existingExerciseIndex = exerciseArray.indexOfFirst { it.exerciseID == this.exerciseID }
        return if (existingExerciseIndex != -1) {
            exerciseArray.toMutableList().apply {
                this[existingExerciseIndex] = this@Exercise
            }.toTypedArray()
        } else {
            exerciseArray + this
        }
    }

    private fun saveExerciseArray(context: Context, cacheKey: String, exerciseArray: Array<Exercise>) {
        cache.setCache(context, cacheKey, exerciseArray)
    }

    private fun showToastMessage(context: Context, isUpdate: Boolean) {
        val messageResId = if (isUpdate) R.string.update_exercise_successful else R.string.save_exercise_successful
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun showToastMessage(context: Context, isUpdate: Boolean, addMessageId: Int, updateMessageId: Int) {
        val messageResId = if (isUpdate) updateMessageId else addMessageId
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
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

    fun toString(context: Context): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        appendBoldText(builder, context.getString(R.string.muscle), muscle)
        appendBoldText(builder, context.getString(R.string.sets), sets.toString())
        appendBoldText(builder, context.getString(R.string.reps), formatRepetitions(repetitions))
        appendBoldText(builder, context.getString(R.string.load), "${load}kg")
        appendBoldText(builder, context.getString(R.string.rest), "${rest}s")
        appendBoldText(builder, context.getString(R.string.cadence), cadence)
        return builder
    }

    private fun appendBoldText(builder: SpannableStringBuilder, label: String, value: String) {
        builder.append("$label: ")
        builder.append(value, StyleSpan(Typeface.BOLD), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append("\n")
    }

    private fun formatRepetitions(repetitions: String): String {
        val repetitionsList = repetitions.split(",")
        return if (repetitionsList.all { it == repetitionsList[0] }) {
            repetitionsList[0]
        } else {
            repetitions
        }
    }
}