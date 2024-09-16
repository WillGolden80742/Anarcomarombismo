package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.example.anarcomarombismo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random

class Exercise(
    var trainingID: Long = 0,
    var linkVideo: String = "",
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

    companion object {
        fun loadList(context: Context, trainingID: Long): Array<Exercise> {
            val cache = Cache()
            val jsonUtil = JSON()

            // Load exercises from cache or create default exercises
            val cacheKey = "Exercicios_$trainingID"
            val exerciseArray = if (cache.hasCache(context, cacheKey)) {
                val cachedData = cache.getCache(context, cacheKey)
                jsonUtil.fromJson(cachedData, Array<Exercise>::class.java)
            } else {
                val random = Random().nextInt(100)
                val defaultExerciseArray = arrayOf(
                    Exercise(trainingID, "", System.currentTimeMillis() + random, "Exercicio", "", 3, "10,10,10", 0.0)
                )
                cache.setCache(context, cacheKey, jsonUtil.toJson(defaultExerciseArray))
                defaultExerciseArray
            }

            // Log exercises for debugging purposes
            for (exercise in exerciseArray) {
                println("Exercício em Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
            }

            // Return the array of exercises
            return exerciseArray
        }
        fun build(
            trainingID: Long,
            linkVideo: String,
            exerciseID: Long,
            name: String,
            muscle: String,
            sets: Int,
            repetitions: String,
            load: Double,
            rest: Int,
            cadence: String
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
    }

    fun save(context: Context): Boolean {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val updatedExerciseArray = updateExerciseArray(exerciseArray)

        saveExerciseArray(context, cacheKey, updatedExerciseArray)
        showToastMessage(context, exerciseID in exerciseArray.map { it.exerciseID })
        return true
    }
    fun remove(context: Context) {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val newExerciseArray = exerciseArray.filter { it.exerciseID != exerciseID }.toTypedArray()

        saveExerciseArray(context, cacheKey, newExerciseArray)
        showToastMessage(context, false, R.string.remove_exercise_successful, R.string.remove_exercise_successful)
    }
    fun load(context: Context, trainingID: Long, exerciseID: Long): Exercise? {
        val cache = Cache()
        val jsonUtil = JSON()
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = if (cache.hasCache(context, cacheKey)) {
            jsonUtil.fromJson(cache.getCache(context, cacheKey), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
        return exerciseArray.find { it.exerciseID == exerciseID }
    }

    fun formatRepetitionsAndCountSets(editTextSets: EditText, editTextRepetitions: EditText) {
        CoroutineScope(Dispatchers.Main).launch {
            val text = editTextRepetitions.text.toString()
            val newText = text.replace(Regex("[^0-9Xx*,]"), "")
            if (text.contains("X") || text.contains("x") || text.contains("*")) {
                handleXFormat(editTextSets, editTextRepetitions, text)
            } else if (text.contains(",")) {
                handleCommaFormat(editTextSets, editTextRepetitions, text)
            } else if (newText != text) {
                editTextRepetitions.setText(newText)
            }
        }
    }

    private fun handleXFormat(editTextSets: EditText, editTextRepetitions: EditText, text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val newText = async {
                text.replace(Regex("[^0-9Xx*]|X{2,}|x{2,}|\\*{2,}"), "")
            }
            val processedText = async {
                val xCount = newText.await().count { it == 'X' || it == 'x' || it == '*' }
                if (xCount > 1) {
                    newText.await().dropLast(1)
                } else {
                    newText.await()
                }
            }
            val numbers = async {
                processedText.await().split(Regex("[Xx*]")).filter { it.isNotEmpty() }
            }
            withContext(Dispatchers.Main) {
                try {
                    editTextSets.setText(numbers.await()[0])
                } catch (e: Exception) {
                    editTextSets.setText("1")
                }
                editTextRepetitions.setText("")
                if (processedText.await() != text) {
                    editTextRepetitions.setText(processedText.await())
                    editTextRepetitions.setSelection(processedText.await().length)
                }
            }
        }
    }

    private fun handleCommaFormat(editTextSets: EditText, editTextRepetitions: EditText, text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val newText = async {
                text.replace(Regex("[^0-9,]|,{2,}"), "")
            }
            val numbers = async {
                newText.await().split(",").filter { it.isNotEmpty() }
            }
            withContext(Dispatchers.Main) {
                editTextSets.setText(numbers.await().size.toString())
                if (newText.await() != text) {
                    editTextRepetitions.setText(newText.await())
                    editTextRepetitions.setSelection(newText.await().length)
                }
            }
        }
    }
    private fun getExerciseArray(context: Context, cacheKey: String): Array<Exercise> {
        return if (cache.hasCache(context, cacheKey)) {
            jsonUtil.fromJson(cache.getCache(context, cacheKey), Array<Exercise>::class.java)
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
        cache.setCache(context, cacheKey, jsonUtil.toJson(exerciseArray))
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


    fun toString(context: Context): String {
        return "${context.getString(R.string.muscle)}: $muscle, \n${context.getString(R.string.sets)}: $sets,\n${context.getString(R.string.reps)}: $repetitions, \n${context.getString(R.string.load)}: $load, \n${context.getString(R.string.rest)}: $rest, \n${context.getString(R.string.cadence)}: $cadence"
    }

    override fun toString(): String {
        return "Muscle: $muscle, \nsets: $sets, \nrepetitions: $repetitions, \nload:$load, \nrest: $rest, \ncadence: '$cadence'"
    }
}
