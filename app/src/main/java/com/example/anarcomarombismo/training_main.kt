package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.JSON
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.TrainingAdapter
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.Training

class training_main : AppCompatActivity(),  TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: Button
    private lateinit var trainingList: ListView
    private lateinit var dailyCaloriesButton: Button
    private var trainingArray = arrayOf<Training>()
    private var jsonUtil = JSON()
    private var cache: Cache? = null
    private val listView: ListView by lazy { findViewById(R.id.trainingList) }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        addTrainingButton = findViewById(R.id.addTrainingButton)
        dailyCaloriesButton = findViewById(R.id.dailyCaloriesButton)
        trainingList = findViewById(R.id.trainingList)
        loadTraining()
        addTrainingButton.setOnClickListener {
            callTraining()
        }
        dailyCaloriesButton.setOnClickListener {
            callDailyCalories()
        }
        //printFood()
    }

    fun callDailyCalories() {
        try {
            startActivity(Intent(this, dailyCalories::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    private fun printFood () {
        try {
            // Lê o conteúdo do arquivo JSON
            //R.raw.nutritional_table
            val jsonContent = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            // Converte o JSON para uma lista de objetos Food
            val foodNutritionList: List<Food> = jsonUtil.fromJson(jsonContent, Array<Food>::class.java).toList()
            // Imprime os resultados
            for (food in foodNutritionList) {
                println(food.toString())
            }
        } catch (e: Exception) {
            println(RuntimeException("Erro ao ler o arquivo JSON: $e"))
        }
    }

    override fun onItemClick(training: Training) {
        // Manipule o clique do item aqui
    }

    // onResume() é chamado toda vez que a Activity volta a ser a principal
    override fun onResume() {
        super.onResume()
        loadTraining()
    }

    private fun callTraining() {
        try {
            startActivity(Intent(this, formTraining::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de adicionar treino: $e")
        }
    }

    private fun loadTraining() {
        cache = Cache()
        if (cache!!.hasCache(this,"Treinos")) {
            val cachedData = cache!!.getCache(this,"Treinos")
            trainingArray = jsonUtil.fromJson(cachedData, Array<Training>::class.java)
            // print every trainings
            for (training in trainingArray) {
                println("Treino em Cache: ${training.trainingID} - ${training.name} - ${training.description}")
            }
        } else {
            trainingArray = arrayOf(
                Training(1,"Treino A", "Peito e Tríceps"),
                Training(2,"Treino B", "Costas e Bíceps"),
                Training(3,"Treino C", "Ombro e Trapézio"),
                Training(4,"Treino D", "Pernas e Panturrilha"),
            )
            for (training in trainingArray) {
                println("Treino fora de Cache: ${training.trainingID} - ${training.name} - ${training.description}")
            }
            cache!!.setCache(this,"Treinos", jsonUtil.toJson(trainingArray))
            dumpExercise()
        }

        val jsonUtil = JSON()
        val trainingArrayJson = jsonUtil.toJson(trainingArray)
        println("Training Array em JSON: $trainingArrayJson")
        val adapter = TrainingAdapter(this, trainingArray, this)
        listView.adapter = adapter
    }

    private fun insertExercisesForTraining(trainingId: Long, exercises: Array<Exercise>) {
        val cache = Cache()
        val jsonUtil = JSON()
        // Adicionar os exercícios do treinamento atual ao cache
        cache.setCache(this, "Exercicios_$trainingId", jsonUtil.toJson(exercises))
    }

    private fun dumpExercise() {

        val trainingExercisesMap = mapOf<Long, Array<Exercise>>(
            1L to arrayOf(
                Exercise(1, 1, "Supino Reto", 4, "8,8,8,8", 100.0),
                Exercise(1, 2, "Supino Inclinado", 3, "10,10,10", 90.0),
                Exercise(1, 3, "Crucifixo com Halteres", 3, "12,12,12", 20.0),
                Exercise(1, 4, "Tríceps Pulley", 4, "10,10,10,10", 50.0),
                Exercise(1, 5, "Tríceps Testa", 3, "12,12,12", 30.0)
            ),
            2L to arrayOf(
                Exercise(2, 1, "Barra Fixa", 4, "6,6,6,6", 0.0),
                Exercise(2, 2, "Remada Curvada", 3, "8,8,8", 0.0),
                Exercise(2, 3, "Pulldown na Polia", 3, "10,10,10", 0.0),
                Exercise(2, 4, "Rosca Direta", 4, "10,10,10,10", 0.0),
                Exercise(2, 5, "Rosca Alternada com Halteres", 3, "12,12,12", 0.0)
            ),
            3L to arrayOf(
                Exercise(3, 1, "Desenvolvimento Militar", 4, "8,8,8,8", 0.0),
                Exercise(3, 2, "Elevação Lateral", 3, "10,10,10", 0.0),
                Exercise(3, 3, "Elevação Frontal com Halteres", 3, "12,12,12", 0.0),
                Exercise(3, 4, "Encolhimento de Ombros com Barra", 4, "10,10,10,10", 0.0),
                Exercise(3, 5, "Encolhimento de Ombros com Halteres", 3, "12,12,12", 0.0)
            ),
            4L to arrayOf(
                Exercise(4, 1, "Agachamento Livre", 4, "10,10,10,10", 0.0),
                Exercise(4, 2, "Leg Press", 3, "12,12,12", 0.0),
                Exercise(4, 3, "Extensão de Pernas", 3, "12,12,12", 0.0),
                Exercise(4, 4, "Flexão Plantar em Pé", 4, "15,15,15,15", 0.0),
                Exercise(4, 5, "Flexão Plantar Sentado", 3, "15,15,15,15", 0.0)
            )
        )

        for ((trainingId, exercises) in trainingExercisesMap) {
            insertExercisesForTraining(trainingId, exercises)
        }
    }
}
