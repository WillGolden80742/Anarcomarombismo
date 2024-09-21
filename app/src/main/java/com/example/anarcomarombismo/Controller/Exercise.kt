package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.PersistentData
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
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
): PersistentData<Exercise> {
    companion object {
        private val cache = Cache()
        private val json = JSON()
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
            val trainingExercisesMap = mapOf(
                1L to arrayOf(
                    Exercise(1, "https://www.youtube.com/watch?v=Pw-zpREJ7xo", 1, context.getString(R.string.dumbbell_press),context.getString(R.string.breastplate),4,"10,10,10,10", 100.0),
                    Exercise(1, "https://www.youtube.com/watch?v=Ky_JXqloq0w", 2, context.getString(R.string.dumbbell_press_30),context.getString(R.string.breastplate),4,"10,10,10,10", 90.0),
                    Exercise(1, "https://youtu.be/0hsnZxrhAY8?si=ZF0VDqFCBmpCkYwZ&t=331", 3,context.getString(R.string.dumbbell_press_less30),context.getString(R.string.breastplate),4,"10,10,10,10", 20.0),
                    Exercise(1, "https://www.youtube.com/watch?v=q_Qs9fdwscs", 4, context.getString(R.string.pullover_with_halter_on_the_black_bench),context.getString(R.string.breastplate),4,"10,10,10,10", 50.0),
                    Exercise(1, "https://www.youtube.com/watch?v=qLtVeISKSeA&t=123s", 5, context.getString(R.string.tr_ceps_testa), context.getString(R.string.triceps),4,"10,10,10,10", 30.0),
                    Exercise(1, "https://www.youtube.com/watch?v=cQ5ae1dHTAQ", 6, context.getString(R.string.tr_ceps_pulley), context.getString(R.string.triceps),4,"10,10,10,10", 0.0),
                    Exercise(1, "https://www.youtube.com/watch?v=XYjcAZFNPnc", 7, context.getString(R.string.triceps_french_on_low_poly), context.getString(R.string.triceps),4,"10,10,10,10", 0.0),
                    Exercise(1, "https://www.youtube.com/watch?v=iZEN4DK5BFM", 8, context.getString(R.string.triceps_bilateral_machine), context.getString(R.string.triceps),4,"10,10,10,10", 0.0)
                ),
                2L to arrayOf(
                    Exercise(2, "https://www.youtube.com/watch?v=vUu_4jBxM1c", 1, context.getString(R.string.front_triangle_pulley), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=YywSCu4Y360", 2, context.getString(R.string.pulley_front_bar), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=gH_nPs_DoQI", 3, context.getString(R.string.articulated_pulley), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=mUgFn3aMAP4", 4, context.getString(R.string.low_stroke), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=FHyZEuRpSg4", 5, context.getString(R.string.biceps_bar_w),  context.getString(R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=ITRfzXEcBz0", 6, context.getString(R.string.hammer_thread_alternate_each_side_counts_as_repeated),context.getString(R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=1lR_dT07wBM", 7, context.getString(R.string.straight_thread_on_rope),context.getString(R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://youtu.be/QtGkO8fRI6c?si=Vh2VoMPC3-0tsjPg&t=366", 8, context.getString(R.string.thread_spider),context.getString(R.string.biceps),4,"10,10,10,10", 0.0)
                ),
                3L to arrayOf(
                    Exercise(3, "https://www.youtube.com/watch?v=eufDL9MmF8A", 1, context.getString(R.string.development_with_halter),context.getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=c7zMmbWkUPw", 2, context.getString(R.string.lateral_elevation),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=kKjjeiXL960", 3, context.getString(R.string.frontal_elevation),context.getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/IwWvZ0rlNXs?si=e9hu1OEBA0ikhvpL&t=45", 4, context.getString(R.string.sitting_lateral_elevation),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=XIJdRoAHHj4", 5, context.getString(R.string.high_paddle_with_bar_w), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 6, context.getString(R.string.shrinkage_with_halter_on_the_side_of_the_body), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 7, context.getString(R.string.shrinkage_with_halter_in_front_of_body), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 8, context.getString(R.string.shrinking_with_body_halter), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=IwWvZ0rlNXs", 9, context.getString(R.string.side_lift_on_inclined_bench),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0)
                ),
                4L to arrayOf(
                    Exercise(4, "https://www.youtube.com/watch?v=emujvqD_Pq8", 1, context.getString(R.string.calves), context.getString(R.string.calves),6,"15,15,15,15,15,15", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=g-73WZ_c6m4", 2, context.getString(R.string.free_squat), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=adPY6cd4h58", 3, context.getString(R.string.leg_press_45), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=pTUfuTLoTQU", 4, context.getString(R.string.flex_chair), context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=I_uBK4DDflU", 5, context.getString(R.string.extension_chair), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=KIoiwCfcTXM", 6, context.getString(R.string.flex_table), context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 7, context.getString(R.string.machine_leg_abduction), context.getString(R.string.glutes),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 8, context.getString(R.string.adduction_leg_on_machine), context.getString(R.string.adductors),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=_6ElJLyBXcE", 9, context.getString(R.string.squat_stiff),context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0)
                )
            )

            for ((trainingId, exercises) in trainingExercisesMap) {
                val cacheKey = "Exercicios_$trainingId"
                cache.setCache(context, cacheKey, json.toJson(exercises))
            }
        }

    }

    override fun save(context: Context): Boolean {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val updatedExerciseArray = updateExerciseArray(exerciseArray)
        saveExerciseArray(context, cacheKey, updatedExerciseArray)
        showToastMessage(context, exerciseID in exerciseArray.map { it.exerciseID })
        return true
    }
    override fun remove(context: Context):Boolean {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = getExerciseArray(context, cacheKey)
        val newExerciseArray = exerciseArray.filter { it.exerciseID != exerciseID }.toTypedArray()
        saveExerciseArray(context, cacheKey, newExerciseArray)
        showToastMessage(context, false, R.string.remove_exercise_successful, R.string.remove_exercise_successful)
        return true
    }
    fun load(context: Context, exerciseID: Long): Exercise? {
        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = if (cache.hasCache(context, cacheKey)) {
            json.fromJson(cache.getCache(context, cacheKey), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
        return exerciseArray.find { it.exerciseID == exerciseID }
    }

    override fun load(context: Context, id: Any): Exercise? {
        return load(context,id as Long)
    }

    override fun loadList(context: Context): List<Exercise> {

        val cacheKey = "Exercicios_$trainingID"
        val exerciseArray = if (cache.hasCache(context, cacheKey)) {
            val cachedData = cache.getCache(context, cacheKey)
            json.fromJson(cachedData, Array<Exercise>::class.java)
        } else {
            val random = Random().nextInt(100)
            val defaultExerciseArray = arrayOf(
                Exercise(trainingID, "", System.currentTimeMillis() + random, "Exercicio", "", 3, "10,10,10", 0.0)
            )
            cache.setCache(context, cacheKey, json.toJson(defaultExerciseArray))
            defaultExerciseArray
        }

        for (exercise in exerciseArray) {
            println("Exercício em Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
        }

        return exerciseArray.toList()
    }

    fun formatRepetitionsAndCountSets(editTextSets: EditText, editTextRepetitions: EditText) {
        CoroutineScope(Dispatchers.Main).launch {
            val text = editTextRepetitions.text.toString()
            // Incluindo o sinal '×' na expressão regular
            val newText = text.replace(Regex("[^0-9Xx×*,]"), "")
            if (text.contains("X") || text.contains("x") || text.contains("*") || text.contains("×")) {
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
                // Incluindo o sinal '×' na expressão regular
                text.replace(Regex("[^0-9Xx×*]|X{2,}|x{2,}|×{2,}|\\*{2,}"), "")
            }
            val processedText = async {
                val xCount = newText.await().count { it == 'X' || it == 'x' || it == '*' || it == '×' }
                if (xCount > 1) {
                    newText.await().dropLast(1)
                } else {
                    newText.await()
                }
            }
            val numbers = async {
                // Incluindo o sinal '×' na expressão regular de split
                processedText.await().split(Regex("[Xx×*]")).filter { it.isNotEmpty() }
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
            json.fromJson(cache.getCache(context, cacheKey), Array<Exercise>::class.java)
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
        cache.setCache(context, cacheKey, json.toJson(exerciseArray))
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
