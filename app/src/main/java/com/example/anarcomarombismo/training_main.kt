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
                Training(1, getString(R.string.treino_a), getString(R.string.peito_e_tr_ceps)),
                Training(2, getString(R.string.treino_b), getString(R.string.costas_e_b_ceps)),
                Training(3, getString(R.string.treino_c), getString(R.string.ombro_e_trap_zio)),
                Training(4, getString(R.string.treino_d), getString(R.string.pernas_e_panturrilha)),
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
                Exercise(1,"https://www.youtube.com/watch?v=R3Kqv2s-VXQ", 1, getString(R.string.supino_reto), 4, "8,8,8,8", 100.0),
                Exercise(1, "https://www.youtube.com/watch?v=XOGNcjXmafQ",2, getString(R.string.supino_inclinado), 3, "10,10,10", 90.0),
                Exercise(1, "https://www.youtube.com/watch?v=ivtmCIHELfE",3, getString(R.string.crucifixo_com_halteres), 3, "12,12,12", 20.0),
                Exercise(1, "https://www.youtube.com/watch?v=WUPk8Gq20cs",4, getString(R.string.tr_ceps_pulley), 4, "10,10,10,10", 50.0),
                Exercise(1, "https://www.youtube.com/watch?v=f7IVPwvq5_o",5, getString(R.string.tr_ceps_testa), 3, "12,12,12", 30.0)
            ),
            2L to arrayOf(
                Exercise(2, "https://www.youtube.com/watch?v=zxfqoXEoUvc",1, getString(R.string.barra_fixa), 4, "6,6,6,6", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=PoFFS_UMf8c",2, getString(R.string.remada_curvada), 3, "8,8,8", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=-74jtRco5kI",3, getString(R.string.pulldown_na_polia), 3, "10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=TtIJUCHLSVM",4, getString(R.string.rosca_direta), 4, "10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=XkHuPKcVhBU",5, getString(R.string.rosca_alternada_com_halteres), 3, "12,12,12", 0.0)
            ),
            3L to arrayOf(
                Exercise(3, "https://www.youtube.com/watch?v=hbbxJbod00k",1, getString(R.string.desenvolvimento_militar), 4, "8,8,8,8", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=y1how8HHmOQ",2, getString(R.string.eleva_o_lateral), 3, "10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=w2NK3GsfzAc",3, getString(R.string.eleva_o_frontal_com_halteres), 3, "12,12,12", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=vAiDKER9I5I",4, getString(R.string.encolhimento_de_ombros_com_barra), 4, "10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=aVjapCzICRI",5, getString(R.string.encolhimento_de_ombros_com_halteres), 3, "12,12,12", 0.0)
            ),
            4L to arrayOf(
                Exercise(4, "https://www.youtube.com/watch?v=fYvifUC5Nac",1, getString(R.string.agachamento_livre), 4, "10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=qL3YKlcdFUw",2, getString(R.string.leg_press), 3, "12,12,12", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=mzfs2LZrFPg",3, getString(R.string.extens_o_de_pernas), 3, "12,12,12", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=FURGALhnSks",4, getString(R.string.flex_o_plantar_em_p), 4, "15,15,15,15", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=1NBoneuGtQo",5, getString(R.string.flex_o_plantar_sentado), 3, "15,15,15,15", 0.0)
            )
        )

        for ((trainingId, exercises) in trainingExercisesMap) {
            insertExercisesForTraining(trainingId, exercises)
        }
    }
}
