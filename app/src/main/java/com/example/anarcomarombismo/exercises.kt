package com.example.anarcomarombismo

import android.app.DatePickerDialog
import com.example.anarcomarombismo.Adapters.ExerciseAdapter
import com.example.anarcomarombismo.Controller.JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Training
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
    private var cache: Cache?   = null
    private var jsonUtil = JSON()
    private var trainingID: Long = 0
    private val dateFormatStored = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)

        trainingID = intent.getLongExtra("trainingID", 0)

        instacieFields()

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

    // onResume()
    override fun onResume() {
        super.onResume()
        loadExercises(trainingID,dateTextView.text.toString())
    }

    fun instacieFields() {
        dateTextView = findViewById(R.id.dateTextView)
        addExerciseButton = findViewById(R.id.addFoodFormButton)
        exerciseList = findViewById(R.id.caloriesFoodList)
        editTraining = findViewById(R.id.removeFoodFormButton)
        descriptionTrainingLabel = findViewById(R.id.descriptionTrainingLabel)
        trainingName = findViewById(R.id.dailyCaloriesTitle)
    }
    private fun selectDate () {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Define a data selecionada no TextView
                val selectedDate =
                    "$selectedDay/${selectedMonth + 1}/$selectedYear" // Mês é base 0, por isso adicionamos 1
                if (selectedDate != dateTextView.text) {
                    // Formata a data no formato 00/00/0000
                    val formattedDate =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        }
                    dateTextView.text = formattedDate
                    loadExercises(trainingID, formattedDate.toString())
                }
            }, year, month, day
        )

        // Define a data máxima como a data atual
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
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
