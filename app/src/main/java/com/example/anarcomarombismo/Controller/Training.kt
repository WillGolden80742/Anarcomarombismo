package com.example.anarcomarombismo.Controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.Controller.Util.ShareFiles
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.trainings
import java.util.Random

class Training(
    var trainingID: Long = 0,
    var name: String = "",
    var description: String = ""
) : DataHandler<Training> {
    private var randomTrainingID = 0L
    data class WorkoutPlan(val trainings: List<Training>,val exercises: List<Exercise>)
    data class WorkoutPlanWithExerciseHistory(
        val trainings: List<Training>,
        val exercises: List<Exercise>,
        val dailyExercises: Map<String, Set<DailyExercises.ExerciseByDate>>
    )

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

        fun export(context: Context): Boolean {
            val trainings = Training().fetchAll(context)
            if (trainings.isEmpty()) {
                Toast.makeText(context,
                    context.getString(R.string.training_not_found), Toast.LENGTH_SHORT).show()
                return false
            }

            val exercises = mutableListOf<Exercise>()
            val dailyExercises = mutableMapOf<String, Set<DailyExercises.ExerciseByDate>>()

            trainings.forEach { training ->
                val exercisesList = Exercise.build(trainingID = training.trainingID).fetchAll(context)
                exercises.addAll(exercisesList)

                // Collect daily exercise history for each exercise
                exercisesList.forEach { exercise ->
                    val exerciseKey = "${exercise.exerciseID}-${exercise.trainingID}-exerciseHistory"
                    val dailyExerciseHistory = DailyExercises(context).getExerciseHistory(exercise)
                    dailyExercises[exerciseKey] = dailyExerciseHistory
                }
            }

            if (exercises.isEmpty()) {
                Toast.makeText(context,
                    context.getString(R.string.exercise_not_found), Toast.LENGTH_SHORT).show()
                return false
            }

            val workoutPlanWithHistory = WorkoutPlanWithExerciseHistory(trainings, exercises, dailyExercises)
            val jsonWorkoutPlan = JSON.toJson(workoutPlanWithHistory)

            return ShareFiles.exportToFile(
                context = context,
                fileName = "WorkoutPlanWithHistory.anarchy3",
                content = jsonWorkoutPlan,
                onSuccess = {},
                onError = {}
            )
        }

        fun import(context: Context, uri: Uri) {
            ShareFiles.importFromFile(
                context = context,
                uri = uri,
                onSuccess = { content ->
                    try {
                        val workoutPlan = parseWorkoutPlanWithHistory(content)
                        saveWorkoutPlanWithHistory(context, workoutPlan)
                        showToastMessage(context, true,
                            R.string.successful_imported_training,
                            R.string.successful_imported_training)
                        val intent = Intent(context,trainings::class.java)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to other import methods if this fails
                        DailyCalories.import(context, uri)
                    }
                },
                onError = {
                    showToastMessage(context, false,
                        R.string.error_file_empty,
                        R.string.error_file_empty)
                }
            )
        }

        private fun parseWorkoutPlanWithHistory(content: String): WorkoutPlanWithExerciseHistory {
            return if (JSON.hasAttribute(content, "dailyExercises")) {
                JSON.fromJson(content, WorkoutPlanWithExerciseHistory::class.java)
            } else {
                val workoutPlan = JSON.fromJson(content, WorkoutPlan::class.java)
                WorkoutPlanWithExerciseHistory(
                    trainings = workoutPlan.trainings,
                    exercises = workoutPlan.exercises,
                    dailyExercises = emptyMap()
                )
            }
        }



        private fun saveWorkoutPlanWithHistory(context: Context, workoutPlan: WorkoutPlanWithExerciseHistory) {
            // Save trainings and exercises like before
            val trainingKey = context.getString(R.string.trainings)
            cache.setCache(context, trainingKey, workoutPlan.trainings)

            workoutPlan.trainings.forEach { training ->
                val trainingID = training.trainingID
                val associatedExercises = workoutPlan.exercises.filter { it.trainingID == trainingID }
                val contextualKey = context.getString(R.string.exercises)
                val cacheKey = "${contextualKey}_$trainingID"
                cache.setCache(context, cacheKey, associatedExercises)
            }

            // Save daily exercise history
            workoutPlan.dailyExercises.forEach { (key, exerciseHistory) ->
                cache.setCache(context, key, exerciseHistory.toList())
            }
        }

        private fun showToastMessage(context: Context, isUpdate: Boolean, addMessageId: Int, updateMessageId: Int) {
            val messageResId = if (isUpdate) updateMessageId else addMessageId
            Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
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





    fun handleImportResult(uri: Uri, context: Context) {
        import(context, uri)
    }
}
