package com.example.anarcomarombismo

import JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.anarcomarombismo.Adapters.ExerciseAdapter
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Training

class exercises : AppCompatActivity() {

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
        addExerciseButton = findViewById(R.id.addExerciseButton)
        exerciseList = findViewById(R.id.exerciseList)
        editTraining = findViewById(R.id.editTrainingButton)
        descriptionTrainingLabel = findViewById(R.id.descriptionTrainingLabel)
        // Obtém o ID do treinamento passado como extra
        trainingID = intent.getLongExtra("trainingID", 0)
        trainingName = findViewById(R.id.trainingName)

        println("ID do treinamento: $trainingID")
        // Carrega os exercícios com base no ID do treinamento
        loadExercises(trainingID)

        addExerciseButton.setOnClickListener {
            callAddExercise()
        }

        editTraining.setOnClickListener {
            editTraining()
        }

        //dumpExercise()
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

    private fun loadExercises(trainingID: Long) {
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
            val exerciseArray = arrayOf(
                Exercise(trainingID,System.currentTimeMillis()+1,"Exercicio", 0, 0, 0.0),
            )
            for (exercise in exerciseArray) {
                println("Exercício fora de Cache: ${exercise.name} - ${exercise.sets} sets, ${exercise.repetitions} reps, ${exercise.load} kg")
            }
            cache!!.setCache(this,"Exercicios_$trainingID", jsonUtil.toJson(exerciseArray))
        }
        val exerciseArray = jsonUtil.fromJson(cache!!.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        val exerciseAdapter = ExerciseAdapter(this, exerciseArray)
        exerciseList.adapter = exerciseAdapter
    }

    private fun insertExercisesForTraining(trainingId: Long, exercises: Array<Exercise>) {
        val cache = Cache()
        val jsonUtil = JSON()

        // Limpar o cache existente para este treinamento
        cache.setCache(this, "Exercicios_$trainingId", "")

        // Adicionar os exercícios do treinamento atual ao cache
        cache.setCache(this, "Exercicios_$trainingId", jsonUtil.toJson(exercises))
    }

    private fun dumpExercise() {
        val cache = Cache()

        // Limpar o cache de todos os treinamentos
        cache.setCache(this, "Treinos", "")

        val trainingExercisesMap = mapOf<Long, Array<Exercise>>(
            1L to arrayOf(
                Exercise(1, 1, "Supino Reto", 4, 8, 100.0),
                Exercise(1, 2, "Supino Inclinado", 3, 10, 90.0),
                Exercise(1, 3, "Crucifixo com Halteres", 3, 12, 20.0),
                Exercise(1, 4, "Tríceps Pulley", 4, 10, 50.0),
                Exercise(1, 5, "Tríceps Testa", 3, 12, 30.0)
            ),
            2L to arrayOf(
                Exercise(2, 1, "Barra Fixa", 4, 6, 0.0),
                Exercise(2, 2, "Remada Curvada", 3, 8, 0.0),
                Exercise(2, 3, "Pulldown na Polia", 3, 10, 0.0),
                Exercise(2, 4, "Rosca Direta", 4, 10, 0.0),
                Exercise(2, 5, "Rosca Alternada com Halteres", 3, 12, 0.0)
            ),
            3L to arrayOf(
                Exercise(3, 1, "Desenvolvimento Militar", 4, 8, 0.0),
                Exercise(3, 2, "Elevação Lateral", 3, 10, 0.0),
                Exercise(3, 3, "Elevação Frontal com Halteres", 3, 12, 0.0),
                Exercise(3, 4, "Encolhimento de Ombros com Barra", 4, 10, 0.0),
                Exercise(3, 5, "Encolhimento de Ombros com Halteres", 3, 12, 0.0)
            ),
            4L to arrayOf(
                Exercise(4, 1, "Agachamento Livre", 4, 10, 0.0),
                Exercise(4, 2, "Leg Press", 3, 12, 0.0),
                Exercise(4, 3, "Extensão de Pernas", 3, 12, 0.0),
                Exercise(4, 4, "Flexão Plantar em Pé", 4, 15, 0.0),
                Exercise(4, 5, "Flexão Plantar Sentado", 3, 12, 0.0)
            )
        )

        for ((trainingId, exercises) in trainingExercisesMap) {
            insertExercisesForTraining(trainingId, exercises)
        }
    }

}
