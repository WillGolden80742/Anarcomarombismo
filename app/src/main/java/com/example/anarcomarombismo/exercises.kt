package com.example.anarcomarombismo

import com.example.anarcomarombismo.Adapters.ExerciseAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Util.Calendaries
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.Forms.formExercise
import com.example.anarcomarombismo.Forms.formTraining
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class exercises : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var addExerciseButton: Button
    private lateinit var editTraining: Button
    private lateinit var exerciseList: ListView
    private lateinit var trainingName: TextView
    private lateinit var descriptionTrainingLabel: TextView
    private var trainingID: Long = 0
    private val dateFormatStored = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        trainingID = intent.getLongExtra("trainingID", 0)

        instantiateFields()

        dateTextView.text = getCurrentDate()

        addExerciseButton.setOnClickListener {
            callAddExercise()
        }

        editTraining.setOnClickListener {
            editTraining()
        }

        dateTextView.setOnClickListener {
            selectDate()
        }
    }
    override fun onResume() {
        super.onResume()
        loadExercises(trainingID,dateTextView.text.toString())
    }

    private fun instantiateFields() {
        dateTextView = findViewById(R.id.dateTextView)
        addExerciseButton = findViewById(R.id.addFoodFormButton)
        exerciseList = findViewById(R.id.caloriesFoodList)
        editTraining = findViewById(R.id.removeFoodFormButton)
        descriptionTrainingLabel = findViewById(R.id.descriptionTrainingLabel)
        trainingName = findViewById(R.id.dailyCaloriesTitle)
    }
    private fun selectDate() {
        val calendar = Calendar.getInstance()
        val maxDate = calendar.timeInMillis
        Calendaries().selectDate(this, dateTextView, maxDate) {
            loadExercises(trainingID, it)
        }
    }
    private fun getCurrentDate(): String {
        val currentDate = Date()
        return dateFormatStored.format(currentDate)
    }
    private fun callAddExercise() {
        try {
            var formExerciseIntent = Intent(this, formExercise::class.java)
            formExerciseIntent.putExtra("trainingID", trainingID)
            startActivity(formExerciseIntent)
        } catch (e: Exception) {
            println("Erro ao chamar a tela de adicionar exercício: $e")
        }
    }

    private fun editTraining() {
        try {
            var formTrainingIntent = Intent(this, formTraining::class.java)
            formTrainingIntent.putExtra("trainingID", trainingID)
            startActivity(formTrainingIntent)
        } catch (e: Exception) {
            println("Erro ao chamar a tela de editar treino: $e")
        }
    }

    private fun loadExercises(trainingID: Long, date: String) {

        Training().load(this, trainingID).also {
            trainingName.text = it.name
            descriptionTrainingLabel.text = it.description
        }

        val exercisesArray = Exercise.loadList(this, trainingID)

        // Update UI elements if necessary
        val exerciseAdapter = ExerciseAdapter(this, exercisesArray, date)
        exerciseList.adapter = exerciseAdapter
    }


}
