package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.Util.JSON
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.TrainingAdapter
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.Training

class training_main : AppCompatActivity(),  TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: Button
    private lateinit var trainingList: ListView
    private lateinit var dailyCaloriesButton: Button
    private var trainingArray = arrayOf<Training>()
    private var json = JSON()
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
            val foodNutritionList: List<Food> = json.fromJson(jsonContent, Array<Food>::class.java).toList()
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
            trainingArray = json.fromJson(cachedData, Array<Training>::class.java)
            // print every trainings
            for (training in trainingArray) {
                println("Treino em Cache: ${training.trainingID} - ${training.name} - ${training.description}")
            }
        } else {
            trainingArray = arrayOf(
                Training(1, getString(R.string.training_a), getString(R.string.chest_and_triceps)),
                Training(2, getString(R.string.training_b), getString(R.string.back_and_biceps)),
                Training(3, getString(R.string.training_c), getString(R.string.shoulder_and_triceps)),
                Training(4, getString(R.string.training_d), getString(R.string.calf_and_legs)),
            )
            for (training in trainingArray) {
                println("Treino fora de Cache: ${training.trainingID} - ${training.name} - ${training.description}")
            }
            cache!!.setCache(this,"Treinos", json.toJson(trainingArray))
            dumpExercise()
        }

        val json = JSON()
        val trainingArrayJson = json.toJson(trainingArray)
        println("Training Array em JSON: $trainingArrayJson")
        val adapter = TrainingAdapter(this, trainingArray, this)
        listView.adapter = adapter
    }

    private fun insertExercisesForTraining(trainingId: Long, exercises: Array<Exercise>) {
        val cache = Cache()
        val json = JSON()
        // Adicionar os exercícios do treinamento atual ao cache
        cache.setCache(this, "Exercicios_$trainingId", json.toJson(exercises))
    }

    private fun dumpExercise() {

        val trainingExercisesMap = mapOf(
            1L to arrayOf(
                Exercise(1, "https://www.youtube.com/watch?v=Pw-zpREJ7xo", 1, getString(R.string.dumbbell_press),getString(R.string.breastplate),4,"10,10,10,10", 100.0),
                Exercise(1, "https://www.youtube.com/watch?v=Ky_JXqloq0w", 2, getString(R.string.dumbbell_press_30),getString(R.string.breastplate),4,"10,10,10,10", 90.0),
                Exercise(1, "https://youtu.be/0hsnZxrhAY8?si=ZF0VDqFCBmpCkYwZ&t=331", 3,getString(R.string.dumbbell_press_less30),getString(R.string.breastplate),4,"10,10,10,10", 20.0),
                Exercise(1, "https://www.youtube.com/watch?v=q_Qs9fdwscs", 4, getString(R.string.pullover_with_halter_on_the_black_bench),getString(R.string.breastplate),4,"10,10,10,10", 50.0),
                Exercise(1, "https://www.youtube.com/watch?v=qLtVeISKSeA&t=123s", 5, getString(R.string.tr_ceps_testa), getString(R.string.triceps),4,"10,10,10,10", 30.0),
                Exercise(1, "https://www.youtube.com/watch?v=cQ5ae1dHTAQ", 6, getString(R.string.tr_ceps_pulley), getString(R.string.triceps),4,"10,10,10,10", 0.0),
                Exercise(1, "https://www.youtube.com/watch?v=XYjcAZFNPnc", 7, getString(R.string.triceps_french_on_low_poly), getString(R.string.triceps),4,"10,10,10,10", 0.0),
                Exercise(1, "https://www.youtube.com/watch?v=iZEN4DK5BFM", 8, getString(R.string.triceps_bilateral_machine), getString(R.string.triceps),4,"10,10,10,10", 0.0)
            ),
            2L to arrayOf(
                Exercise(2, "https://www.youtube.com/watch?v=vUu_4jBxM1c", 1, getString(R.string.front_triangle_pulley), getString(R.string.back),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=YywSCu4Y360", 2, getString(R.string.pulley_front_bar), getString(R.string.back),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=gH_nPs_DoQI", 3, getString(R.string.articulated_pulley), getString(R.string.back),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=mUgFn3aMAP4", 4, getString(R.string.low_stroke), getString(R.string.back),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=FHyZEuRpSg4", 5, getString(R.string.biceps_bar_w),  getString(R.string.biceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=ITRfzXEcBz0", 6, getString(R.string.hammer_thread_alternate_each_side_counts_as_repeated),getString(R.string.biceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=1lR_dT07wBM", 7, getString(R.string.straight_thread_on_rope),getString(R.string.biceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://youtu.be/QtGkO8fRI6c?si=Vh2VoMPC3-0tsjPg&t=366", 8, getString(R.string.thread_spider),getString(R.string.biceps),4,"10,10,10,10", 0.0)
            ),
            3L to arrayOf(
                Exercise(3, "https://www.youtube.com/watch?v=eufDL9MmF8A", 1, getString(R.string.development_with_halter),getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=c7zMmbWkUPw", 2, getString(R.string.lateral_elevation),getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=kKjjeiXL960", 3, getString(R.string.frontal_elevation),getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/IwWvZ0rlNXs?si=e9hu1OEBA0ikhvpL&t=45", 4, getString(R.string.sitting_lateral_elevation),getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=XIJdRoAHHj4", 5, getString(R.string.high_paddle_with_bar_w), getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 6, getString(R.string.shrinkage_with_halter_on_the_side_of_the_body), getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 7, getString(R.string.shrinkage_with_halter_in_front_of_body), getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 8, getString(R.string.shrinking_with_body_halter), getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=IwWvZ0rlNXs", 9, getString(R.string.side_lift_on_inclined_bench),getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0)
            ),
            4L to arrayOf(
                Exercise(4, "https://www.youtube.com/watch?v=emujvqD_Pq8", 1, getString(R.string.calves), getString(R.string.calves),6,"15,15,15,15,15,15", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=g-73WZ_c6m4", 2, getString(R.string.free_squat), getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=adPY6cd4h58", 3, getString(R.string.leg_press_45), getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=pTUfuTLoTQU", 4, getString(R.string.flex_chair), getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=I_uBK4DDflU", 5, getString(R.string.extension_chair), getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=KIoiwCfcTXM", 6, getString(R.string.flex_table), getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 7, getString(R.string.machine_leg_abduction), getString(R.string.glutes),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 8, getString(R.string.adduction_leg_on_machine), getString(R.string.adductors),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=_6ElJLyBXcE", 9, getString(R.string.squat_stiff),getString(R.string.thigh_back),4,"10,10,10,10", 0.0)
            )
        )

        for ((trainingId, exercises) in trainingExercisesMap) {
            insertExercisesForTraining(trainingId, exercises)
        }
    }
}
