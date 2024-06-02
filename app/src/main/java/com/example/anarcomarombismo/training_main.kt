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

        val trainingExercisesMap = mapOf(
            1L to arrayOf(
                Exercise(1, "https://www.youtube.com/watch?v=Pw-zpREJ7xo", 1, getString(R.string.dumbbell_press),getString(R.string.peitoral),4,"10,10,10,10", 100.0),
                Exercise(1, "https://www.youtube.com/watch?v=Ky_JXqloq0w", 2, getString(R.string.dumbbell_press_30),getString(R.string.peitoral),4,"10,10,10,10", 90.0),
                Exercise(1, "https://youtu.be/0hsnZxrhAY8?si=ZF0VDqFCBmpCkYwZ&t=331", 3,getString(R.string.dumbbell_press_less30),getString(R.string.peitoral),4,"10,10,10,10", 20.0),
                Exercise(1, "https://www.youtube.com/watch?v=q_Qs9fdwscs", 4, getString(R.string.pullover_com_halter_deitado_no_banco_reto),getString(R.string.peitoral),4,"10,10,10,10", 50.0),
                Exercise(1, "https://www.youtube.com/watch?v=qLtVeISKSeA&t=123s", 5, getString(R.string.tr_ceps_testa), getString(R.string.tr_ceps),4,"10,10,10,10", 30.0),
                Exercise(1, "https://www.youtube.com/watch?v=cQ5ae1dHTAQ", 6, getString(R.string.tr_ceps_pulley), getString(R.string.tr_ceps),4,"10,10,10,10", 0.0),
                Exercise(1, "https://www.youtube.com/watch?v=XYjcAZFNPnc", 7, getString(R.string.tr_ceps_franc_s_na_polia_baixa), getString(R.string.tr_ceps),4,"10,10,10,10", 0.0),
                Exercise(1, "https://www.youtube.com/watch?v=iZEN4DK5BFM", 8, getString(R.string.tr_ceps_bilateral_m_quina), getString(R.string.tr_ceps),4,"10,10,10,10", 0.0)
            ),
            2L to arrayOf(
                Exercise(2, "https://www.youtube.com/watch?v=vUu_4jBxM1c", 1, getString(R.string.pulley_frente_tri_ngulo), getString(R.string.dorsal),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=YywSCu4Y360", 2, getString(R.string.pulley_frente_barra), getString(R.string.dorsal),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=gH_nPs_DoQI", 3, getString(R.string.pulley_articulado), getString(R.string.dorsal),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=mUgFn3aMAP4", 4, getString(R.string.remada_baixa), getString(R.string.dorsal),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=FHyZEuRpSg4", 5, getString(R.string.b_ceps_barra_w),  getString(R.string.b_ceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=ITRfzXEcBz0", 6, getString(R.string.rosca_martelo_alternado_cada_lado_conta_como_repeti_o),getString(R.string.b_ceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://www.youtube.com/watch?v=1lR_dT07wBM", 7, getString(R.string.rosca_direta_na_corda),getString(R.string.b_ceps),4,"10,10,10,10", 0.0),
                Exercise(2, "https://youtu.be/QtGkO8fRI6c?si=Vh2VoMPC3-0tsjPg&t=366", 8, getString(R.string.rosca_spider),getString(R.string.b_ceps),4,"10,10,10,10", 0.0)
            ),
            3L to arrayOf(
                Exercise(3, "https://www.youtube.com/watch?v=eufDL9MmF8A", 1, getString(R.string.desenvolvimento_com_halteres),getString(R.string.delt_ides_anterior),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=c7zMmbWkUPw", 2, getString(R.string.eleva_o_lateral),getString(R.string.delt_ides_lateral),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=kKjjeiXL960", 3, getString(R.string.eleva_o_frontal),getString(R.string.delt_ides_anterior),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/IwWvZ0rlNXs?si=e9hu1OEBA0ikhvpL&t=45", 4, getString(R.string.eleva_o_lateral_sentado),getString(R.string.delt_ides_lateral),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=XIJdRoAHHj4", 5, getString(R.string.remada_alta_com_barra_w), getString(R.string.trap_zio),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 6, getString(R.string.encolhimento_com_halter_na_lateral_do_corpo), getString(R.string.trap_zio),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 7, getString(R.string.encolhimento_com_halter_na_frente_do_corpo), getString(R.string.trap_zio),4,"10,10,10,10", 0.0),
                Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 8, getString(R.string.encolhimento_com_halter_detr_s_do_corpo), getString(R.string.trap_zio),4,"10,10,10,10", 0.0),
                Exercise(3, "https://www.youtube.com/watch?v=IwWvZ0rlNXs", 9, getString(R.string.eleva_o_lateral_no_banco_inclinado),getString(R.string.delt_ides_lateral),4,"10,10,10,10", 0.0)
            ),
            4L to arrayOf(
                Exercise(4, "https://www.youtube.com/watch?v=emujvqD_Pq8", 1, getString(R.string.panturrilhas), getString(R.string.panturrilhas),6,"15,15,15,15,15,15", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=g-73WZ_c6m4", 2, getString(R.string.agachamento_livre), getString(R.string.quadr_ceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=adPY6cd4h58", 3, getString(R.string.leg_press_45), getString(R.string.quadr_ceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=pTUfuTLoTQU", 4, getString(R.string.cadeira_flexora), getString(R.string.posterior_de_coxa),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=I_uBK4DDflU", 5, getString(R.string.cadeira_extensora), getString(R.string.quadr_ceps),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=KIoiwCfcTXM", 6, getString(R.string.mesa_flexora), getString(R.string.posterior_de_coxa),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 7, getString(R.string.abdu_o_de_perna_na_m_quina), getString(R.string.gl_teos),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 8, getString(R.string.adu_o_de_perna_na_m_quina), getString(R.string.adutores),4,"10,10,10,10", 0.0),
                Exercise(4, "https://www.youtube.com/watch?v=_6ElJLyBXcE", 9, getString(R.string.agachamento_stiff),getString(R.string.posterior_de_coxa),4,"10,10,10,10", 0.0)
            )
        )

        for ((trainingId, exercises) in trainingExercisesMap) {
            insertExercisesForTraining(trainingId, exercises)
        }
    }
}
