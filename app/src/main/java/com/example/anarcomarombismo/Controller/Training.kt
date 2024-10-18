package com.example.anarcomarombismo.Controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.mainActivity
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Random

class Training(
    var trainingID: Long = 0,
    var name: String = "",
    var description: String = ""
) : DataHandler<Training> {
    private var randomTrainingID = 0L
    data class WorkoutPlan(val trainings: List<Training>,val exercises: List<Exercise>)

    companion object {
        private val cache = Cache()

        fun build(
            trainingID: Long = 0,
            name: String = "",
            description: String = ""
        ): Training {
            return Training().apply {
                this.trainingID = trainingID
                this.randomTrainingID = if (trainingID > 0) trainingID else generateTrainingID()
                this.name = name
                this.description = description
            }
        }

        private fun hasTraining(context: Context): Boolean {
            val contextualKey = context.getString(R.string.trainings)
            return cache.hasCache(context, contextualKey)
        }
    }

    private fun generateTrainingID(): Long {
        return Random().nextInt(100) + System.currentTimeMillis()
    }

    override fun save(context: Context): Boolean {
        val contextualKey = context.getString(R.string.trainings)
        val trainingArray = cache.getCache(context, contextualKey, Array<Training>::class.java)

        val updatedTrainingArray = if (trainingID > 0) {
            trainingArray.map {
                if (it.trainingID == trainingID) {
                    it.apply {
                        this.name = this@Training.name
                        this.description = this@Training.description
                    }
                } else it
            }
        } else {
            trainingArray.plus(
                Training(
                    trainingID = randomTrainingID,
                    name = this.name,
                    description = this.description
                )
            )
        }

        cache.setCache(context, contextualKey, updatedTrainingArray)

        val message = if (trainingID > 0) {
            context.getString(R.string.update_training_successful)
        } else {
            context.getString(R.string.save_training_successful)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        return true
    }

    override fun remove(context: Context): Boolean {
        val contextualKey = context.getString(R.string.trainings)
        val trainingArray = cache.getCache(context, contextualKey, Array<Training>::class.java)

        val updatedTrainingArray = trainingArray.filter { it.trainingID != trainingID }
        if (updatedTrainingArray.size < trainingArray.size) {
            cache.setCache(context, contextualKey, updatedTrainingArray)
            Toast.makeText(context, context.getString(R.string.remove_training_successful), Toast.LENGTH_SHORT).show()
            return true
        }
        Toast.makeText(context, context.getString(R.string.remove_training_error), Toast.LENGTH_SHORT).show()
        return false
    }

    override fun fetchById(context: Context, id: Any): Training {
        val contextualKey = context.getString(R.string.trainings)
        val trainingArray = cache.getCache(context, contextualKey, Array<Training>::class.java)

        val training = trainingArray.find { it.trainingID == id as Long }
        if (training != null) {
            this.name = training.name
            this.description = training.description
        } else {
            this.name = context.getString(R.string.training)
        }
        return this
    }

    override fun fetchAll(context: Context): List<Training> {
        val contextualKey = context.getString(R.string.trainings)
        val trainingArray: List<Training>
        if (hasTraining(context)) {
            trainingArray = cache.getCache(context, contextualKey, Array<Training>::class.java).toList()
            for (training in trainingArray) {
                println("Treino em Cache: ${training.trainingID} - ${training.name} - ${training.description}")
            }
        } else {
            trainingArray = listOf(
                Training(1, context.getString(R.string.training_a), context.getString(R.string.chest_and_triceps)),
                Training(2, context.getString(R.string.training_b), context.getString(R.string.back_and_biceps)),
                Training(3, context.getString(R.string.training_c), context.getString(R.string.shoulder_and_triceps)),
                Training(4, context.getString(R.string.training_d), context.getString(R.string.calf_and_legs))
            )
            cache.setCache(context, contextualKey, trainingArray)
            Exercise.dumpExercise(context)
        }
        return trainingArray.toList()
    }

    private fun showToastMessage(context: Context, isUpdate: Boolean) {
        val messageResId = if (isUpdate) R.string.update_exercise_successful else R.string.save_exercise_successful
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun showToastMessage(context: Context, isUpdate: Boolean, addMessageId: Int, updateMessageId: Int) {
        val messageResId = if (isUpdate) updateMessageId else addMessageId
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }



    fun export(context: Context): Boolean {
        val trainings = fetchAll(context)
        if (trainings.isEmpty()) {
            Toast.makeText(context,
                context.getString(R.string.training_not_found), Toast.LENGTH_SHORT).show()
            return false
        }

        val exercises = mutableListOf<Exercise>()
        trainings.forEach { training ->
            val exercisesList = Exercise.build(trainingID = training.trainingID).fetchAll(context)
            exercises.addAll(exercisesList)
        }

        if (exercises.isEmpty()) {
            Toast.makeText(context,
                context.getString(R.string.exercise_not_found), Toast.LENGTH_SHORT).show()
            return false
        }

        val workoutPlan = WorkoutPlan(trainings, exercises)
        val jsonWorkoutPlan = JSON.toJson(workoutPlan)
        val fileName = "WorkoutPlan.anarchy3"

        return try {
            val file = createFile(context, fileName)
            writeToFile(file, jsonWorkoutPlan)
            shareFile(context, file)
            true
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.file_export_error), Toast.LENGTH_SHORT).show()
            false
        }
    }

    @Throws(IOException::class)
    private fun createFile(context: Context, fileName: String): File {
        return File(context.filesDir, fileName).apply {
            if (!exists()) createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(file: File, content: String) {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun import(context: Context) {
        // Cria um ActivityResultLauncher para lidar com o resultado do seletor de arquivos
        var launcher: ActivityResultLauncher<Array<String>>
        try {
            launcher =
                (context as mainActivity).registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                    uri?.let {
                        handleImportResult(it, context)
                    } ?: showToastMessage(
                        context,
                        false,
                        R.string.error_no_file_selected,
                        R.string.error_no_file_selected
                    )
                }

            launcher.launch(arrayOf("application/octet-stream"))
        } catch (e: Exception) {
            println("Error activity: $e")
        }
    }

    fun handleImportResult(uri: Uri, context: Context) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = StringBuilder()
                reader.forEachLine { content.append(it).append("\n") }
                reader.close()
                val workoutPlan = parseWorkoutPlan(content.toString())
                saveWorkoutPlan(context, workoutPlan)
                showToastMessage(context, true,R.string.successful_imported_training, R.string.successful_imported_training)
            } else {
                showToastMessage(context, false, R.string.error_file_empty, R.string.error_file_empty)
            }
        } catch (e: Exception) {
            Log.e("Exercise", "Error reading file", e)
            showToastMessage(context, false, R.string.error_reading_file, R.string.error_reading_file)
        }
    }

    // A hypothetical function to parse the file content into a WorkoutPlan object
    private fun parseWorkoutPlan(content: String): WorkoutPlan {
        return JSON.fromJson(content, WorkoutPlan::class.java)
    }


    private fun saveWorkoutPlan(context: Context, workoutPlan: WorkoutPlan) {
        // Salve os treinos no cache
        val trainingKey = context.getString(R.string.trainings)
        cache.setCache(context, trainingKey, workoutPlan.trainings)

        // Salve os exercícios associados a cada treino
        workoutPlan.trainings.forEach { training ->
            val trainingID = training.trainingID

            // Filtre os exercícios que pertencem ao treino atual (trainingID)
            val associatedExercises = workoutPlan.exercises.filter { it.trainingID == trainingID }

            // Crie uma chave de cache específica para este treino
            val contextualKey = context.getString(R.string.exercises)
            val cacheKey = "${contextualKey}_$trainingID"

            // Salve os exercícios associados ao treino no cache
            cache.setCache(context, cacheKey, associatedExercises)
        }
    }

}
