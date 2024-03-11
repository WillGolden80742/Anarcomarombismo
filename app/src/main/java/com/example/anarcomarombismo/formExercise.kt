package com.example.anarcomarombismo

import JSON
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise

class formExercise : AppCompatActivity() {

    private lateinit var editTextExerciseName: EditText
    private lateinit var editTextSets: EditText
    private lateinit var editTextRepetitions: EditText
    private lateinit var editTextLoad: EditText
    private lateinit var editTextRest: EditText // Novo campo para repouso
    private lateinit var editTextCadence: EditText // Novo campo para cadência
    private lateinit var addExerciseButton: Button
    private lateinit var removeExerciseButton: Button
    private var trainingID: Long = 0
    private var exerciseID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_exercise)
        instantiateFields()
        trainingID = intent.getLongExtra("trainingID", 0)
        exerciseID = intent.getLongExtra("exerciseID", 0)
        println("ID do exercio: $exerciseID")
        loadExerciseIfExistInCache()
        addExerciseButton.setOnClickListener {
            saveExercise()
        }
        removeExerciseButton.setOnClickListener {
            removeExercise()
        }
    }

    private fun instantiateFields() {
        editTextExerciseName = findViewById(R.id.editTextExerciseName)
        editTextSets = findViewById(R.id.editTextSets)
        editTextRepetitions = findViewById(R.id.editTextRepetitions)
        editTextLoad = findViewById(R.id.editTextLoad)
        editTextRest = findViewById(R.id.editTextRest) // Inicialização do novo campo para repouso
        editTextCadence = findViewById(R.id.editTextCadence) // Inicialização do novo campo para cadência
        addExerciseButton = findViewById(R.id.addExerciseButton)
        removeExerciseButton = findViewById(R.id.editTrainingButton)
    }

    private fun loadExerciseIfExistInCache() {
        val cache = Cache()
        val jsonUtil = JSON()
        if (exerciseID > 0) {
            val exerciseArray = jsonUtil.fromJson(cache.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)

            for (exercise in exerciseArray) {
                if (exercise.exerciseID == exerciseID) {
                    editTextExerciseName.setText(exercise.name)
                    editTextSets.setText(exercise.sets.toString())
                    editTextRepetitions.setText(exercise.repetitions.toString())
                    editTextLoad.setText(exercise.load.toString())
                    editTextRest.setText(exercise.rest.toString()) // Definir o valor do campo de repouso
                    editTextCadence.setText(exercise.cadence) // Definir o valor do campo de cadência
                    addExerciseButton.text = "Atualizar Exercício"
                }
            }
        } else {
            removeExerciseButton.isVisible = false
        }
    }

    override fun onResume() {
        super.onResume()
        loadExerciseIfExistInCache()
    }

    private fun saveExercise() {
        val cache = Cache()
        val jsonUtil = JSON()
        val defaultCadence = getString(R.string.default_cadence)
        val defaultRest = getString(R.string.default_rest)
        val defaultReps = getString(R.string.default_reps)
        val defaultSets = getString(R.string.default_sets)
        val exerciseHint = getString(R.string.exercise_hint)

        val exerciseArray = if (cache.hasCache(this, "Exercicios_$trainingID")) {
            jsonUtil.fromJson(cache.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }

        var exercise = Exercise(
            trainingID,
            exerciseID,
            editTextExerciseName.text.toString().takeIf { it.isNotEmpty() } ?: exerciseHint,
            editTextSets.text.toString().toIntOrNull() ?: defaultSets.toInt(),
            editTextRepetitions.text.toString().toIntOrNull() ?: defaultReps.toInt(),
            editTextLoad.text.toString().toDoubleOrNull() ?: 0.0,
            editTextRest.text.toString().toIntOrNull() ?: defaultRest.toInt(),
            editTextCadence.text.toString().takeIf { it.isNotEmpty() } ?: defaultCadence
        )

        val newExerciseArray = if (exerciseID > 0) {
            exerciseArray.map {
                if (it.exerciseID == exerciseID) {
                    exercise
                } else {
                    it
                }
            }.toTypedArray()
        } else {
            exercise.exerciseID = System.currentTimeMillis()
            exerciseArray.plus(exercise)
        }

        cache.setCache(this, "Exercicios_$trainingID", jsonUtil.toJson(newExerciseArray))
        if (exerciseID > 0) {
            Toast.makeText(this, getString(R.string.update_exercise_successful), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.save_exercise_successful), Toast.LENGTH_SHORT).show()
        }
        finish()
    }


    private fun removeExercise() {
        val cache = Cache()
        val jsonUtil = JSON()
        val exerciseArray = if (cache.hasCache(this, "Exercicios_$trainingID")) {
            jsonUtil.fromJson(cache.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
        val newExerciseArray = exerciseArray.filter {
            it.exerciseID != exerciseID
        }.toTypedArray()
        cache.setCache(this, "Exercicios_$trainingID", jsonUtil.toJson(newExerciseArray))
        Toast.makeText(this, getString(R.string.remove_exercise_successful), Toast.LENGTH_SHORT).show()
        finish()
    }
}
