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
import java.util.Locale
import java.util.Random

class exercises : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var addExerciseButton: Button
    private lateinit var editTraining: Button
    private lateinit var exerciseList: ListView
    private lateinit var trainingName: TextView
    private lateinit var descriptionTrainingLabel: TextView
    private var cache: Cache? = null
    private var jsonUtil = JSON()
    private var trainingID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        dateTextView = findViewById(R.id.dateTextView)
        addExerciseButton = findViewById(R.id.addFoodFormButton)
        exerciseList = findViewById(R.id.caloriesFoodList)
        editTraining = findViewById(R.id.removeFoodFormButton)
        descriptionTrainingLabel = findViewById(R.id.descriptionTrainingLabel)
        // Obtém o ID do treinamento passado como extra
        trainingID = intent.getLongExtra("trainingID", 0)
        trainingName = findViewById(R.id.dailyCaloriesTitle)

        println("ID do treinamento: $trainingID")
        // Carrega os exercícios com base no ID do treinamento
        loadExercises(trainingID)

        addExerciseButton.setOnClickListener {
            callAddExercise()
        }

        editTraining.setOnClickListener {
            editTraining()
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        dateTextView.setOnClickListener {
            // Cria um DatePickerDialog para selecionar a data
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
    }

    // onResume()
    override fun onResume() {
        super.onResume()
        loadExercises(trainingID)
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

    private fun loadExercises(trainingID: Long,date:String) {
        cache = Cache()
        // get treino name from cache to show in the top of the screen
        if (cache!!.hasCache(this,"Treinos")) {
            val cachedData = cache!!.getCache(this,"Treinos")
            val trainingArray = jsonUtil.fromJson(cachedData, Array<Training>::class.java)
            for (training in trainingArray) {
                if (training.trainingID == trainingID) {
                    trainingName.text = training.name
                    descriptionTrainingLabel.text = training.description

                }
            }
        }
        if (cache!!.hasCache(this,"Exercicios_$trainingID")) {
            val cachedData = cache!!.getCache(this,"Exercicios_$trainingID")
            val exerciseArray = jsonUtil.fromJson(cachedData, Array<Exercise>::class.java)
            // Printa todos os exercícios
            for (exercise in exerciseArray) {
                println("Exercício em Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
            }
        } else {
            val random = Random().nextInt(100)
            val exerciseArray = arrayOf(
                Exercise(trainingID,"",System.currentTimeMillis()+random,"Exercicio", "",3, "10,10,10", 0.0),
            )
            for (exercise in exerciseArray) {
                println("Exercício fora de Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
            }
            cache!!.setCache(this,"Exercicios_$trainingID", jsonUtil.toJson(exerciseArray))
        }
        val exerciseArray = jsonUtil.fromJson(cache!!.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        val exerciseAdapter = ExerciseAdapter(this, exerciseArray,date)
        exerciseList.adapter = exerciseAdapter
    }

    private fun loadExercises(trainingID: Long) {
        loadExercises(trainingID,"")
    }

}
