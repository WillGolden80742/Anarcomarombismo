package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.util.Random

class Training(
    var trainingID: Long = 0,
    var name: String = "",
    var description: String = ""
) {
    private var randomTrainingID = 0L
    companion object {
        private val cache = Cache()
        private val json = JSON()
        fun build(
            trainingID: Long = 0,
            name: String,
            description: String
        ): Training {
            return Training().apply {
                this.trainingID = trainingID
                this.randomTrainingID = if (trainingID > 0) trainingID else generateTrainingID()
                this.name = name
                this.description = description
            }
        }
        private fun hasTraining(context: Context): Boolean {
            return cache.hasCache(context, "Treinos")
        }
        fun loadList(context: Context): Array<Training> {
            val trainingArray: Array<Training>
            if (hasTraining(context)) {
                val cachedData = cache.getCache(context, "Treinos")
                trainingArray = json.fromJson(cachedData, Array<Training>::class.java)
                for (training in trainingArray) {
                    println("Treino em Cache: ${training.trainingID} - ${training.name} - ${training.description}")
                }
            } else {
                trainingArray = arrayOf(
                    Training(1, context.getString(R.string.training_a), context.getString(R.string.chest_and_triceps)),
                    Training(2, context.getString(R.string.training_b), context.getString(R.string.back_and_biceps)),
                    Training(3, context.getString(R.string.training_c), context.getString(R.string.shoulder_and_triceps)),
                    Training(4, context.getString(R.string.training_d), context.getString(R.string.calf_and_legs))
                )
                cache.setCache(context, "Treinos", json.toJson(trainingArray))
                Exercise.dumpExercise(context)
            }
            return trainingArray
        }
    }
    private fun generateTrainingID(): Long {
        return Random().nextInt(100)+System.currentTimeMillis()
    }
    fun save(context: Context): Boolean {
        val trainingArray = json.fromJson(cache.getCache(context, "Treinos"), Array<Training>::class.java)
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
        cache.setCache(context, "Treinos", json.toJson(updatedTrainingArray))
        val message = if (trainingID > 0) {
            context.getString(R.string.update_training_successful)
        } else {
            context.getString(R.string.save_training_successful)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        return true
    }

    fun remove(context: Context): Boolean {
        val trainingArray = json.fromJson(cache.getCache(context, "Treinos"), Array<Training>::class.java)
        val updatedTrainingArray = trainingArray.filter { it.trainingID != trainingID }
        if (updatedTrainingArray.size < trainingArray.size) {
            cache.setCache(context, "Treinos", json.toJson(updatedTrainingArray))
            Toast.makeText(context, context.getString(R.string.remove_training_successful), Toast.LENGTH_SHORT).show()
            return true
        }
        Toast.makeText(context, context.getString(R.string.remove_training_error), Toast.LENGTH_SHORT).show()
        return false
    }

    fun load(context: Context, trainingID: Long):Training {
        val trainingArray = json.fromJson(cache.getCache(context, "Treinos"), Array<Training>::class.java)
        val training = trainingArray.find { it.trainingID == trainingID }
        if (training != null) {
            this.name = training.name
            this.description = training.description
        } else {
            this.name = context.getString(R.string.training)
        }
        return this
    }
}
