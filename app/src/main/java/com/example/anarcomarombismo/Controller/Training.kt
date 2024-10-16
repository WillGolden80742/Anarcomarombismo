package com.example.anarcomarombismo.Controller

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.io.File
import java.io.FileOutputStream
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

    fun export(context: Context): Boolean {
        val trainings = fetchAll(context)
        var exercises = listOf(Exercise())
        for (training in trainings) {
            val exercisesList = Exercise.build(trainingID = training.trainingID).fetchAll(context)
            exercises = exercises.plus(exercisesList)
        }
        exercises = exercises.drop(1)
        val workoutPlan = WorkoutPlan(trainings, exercises)
        val jsonWorkoutPlan = JSON.toJson(workoutPlan)
        val fileName = "WorkoutPlan.anarchy3"
        try {
            val file = File(context.filesDir, fileName)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(jsonWorkoutPlan.toByteArray())
            fileOutputStream.close()
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/json"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context,
                context.getString(R.string.file_export_error), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
