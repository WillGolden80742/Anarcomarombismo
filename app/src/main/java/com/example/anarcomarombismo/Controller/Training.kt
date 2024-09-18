package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.util.Random

class Training(
    val trainingID: Long = 0,
    var name: String = "",
    var description: String = ""
) {

    fun save(context: Context): Boolean {
        val cache = Cache()
        val json = JSON()
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
            val random = Random().nextInt(100)+System.currentTimeMillis()
            val newTraining = Training(
                trainingID = random,
                name = this.name,
                description = this.description
            )
            trainingArray.plus(newTraining)
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
        val cache = Cache()
        val json = JSON()
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
        val cache = Cache()
        val json = JSON()
        val trainingArray = json.fromJson(cache.getCache(context, "Treinos"), Array<Training>::class.java)
        val training = trainingArray.find { it.trainingID == trainingID }
        if (training != null) {
            this.name = training.name
            this.description = training.description
        }
        return this
    }
}
