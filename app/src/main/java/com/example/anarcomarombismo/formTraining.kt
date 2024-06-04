package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Training
import java.util.Random

class formTraining : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var description: EditText
    private lateinit var save: Button
    private lateinit var removeTraining: Button
    private var trainingID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_training)

        name = findViewById(R.id.name)
        description = findViewById(R.id.description)
        save = findViewById(R.id.save)
        removeTraining = findViewById(R.id.removeTraining)

        trainingID = intent.getLongExtra("trainingID", 0)

        save.setOnClickListener {
            saveTraining()
        }

        if (trainingID == 0L) {
            removeTraining.isVisible = false
        }

        removeTraining.setOnClickListener {
            removeTraining()
        }

        loadTrainingIfExistInCache()

    }


    private fun loadTrainingIfExistInCache() {
        val cache = Cache()
        val jsonUtil = JSON()
        if (trainingID > 0) {
            val trainingArray = jsonUtil.fromJson(cache.getCache(this, "Treinos"), Array<Training>::class.java)
            for (training in trainingArray) {
                if (training.trainingID == trainingID) {
                    name.setText(training.name)
                    description.setText(training.description)
                }
            }
            save.text = getString(R.string.update_training)
        }
    }

    private fun saveTraining() {
        val name = name.text.toString()
        val description = description.text.toString()
        val cache = Cache()
        val jsonUtil = JSON()
        if (trainingID > 0) {
            val trainingArray = jsonUtil.fromJson(cache.getCache(this, "Treinos"), Array<Training>::class.java)
            for (training in trainingArray) {
                if (training.trainingID == trainingID) {
                    training.name = name
                    training.description = description
                }
            }
            cache.setCache(this, "Treinos", jsonUtil.toJson(trainingArray))
            Toast.makeText(this, getString(R.string.update_training_successful), Toast.LENGTH_SHORT).show()
        } else {
            val random = Random().nextInt(100)
            val training = Training(System.currentTimeMillis()+random, name, description)
            val trainingArray = jsonUtil.fromJson(cache.getCache(this, "Treinos"), Array<Training>::class.java)
            val newTrainingArray = trainingArray.plus(training)
            cache.setCache(this, "Treinos", jsonUtil.toJson(newTrainingArray))
            Toast.makeText(this, getString(R.string.save_training_successful), Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun removeTraining() {
        val cache = Cache()
        val jsonUtil = JSON()
        val trainingArray = jsonUtil.fromJson(cache.getCache(this, "Treinos"), Array<Training>::class.java)
        val newTrainingArray = trainingArray.filter { it.trainingID != trainingID }
        cache.setCache(this, "Treinos", jsonUtil.toJson(newTrainingArray))
        Toast.makeText(this, getString(R.string.remove_training_successful), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, training_main::class.java)
        startActivity(intent)
    }
}
