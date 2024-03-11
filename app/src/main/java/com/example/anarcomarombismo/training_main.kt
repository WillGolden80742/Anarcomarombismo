package com.example.anarcomarombismo

import JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.example.anarcomarombismo.Adapters.TrainingAdapter
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Training

class training_main : AppCompatActivity(),  TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: Button
    private lateinit var trainingList: ListView
    private var trainingArray = arrayOf<Training>()
    private var jsonUtil = JSON()
    private var cache: Cache? = null
    private val listView: ListView by lazy { findViewById(R.id.trainingList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        addTrainingButton = findViewById(R.id.addTrainingButton)
        trainingList = findViewById(R.id.trainingList)
        loadTraining()
        addTrainingButton.setOnClickListener {
            callTraining()
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
        }

        val jsonUtil = JSON()
        val trainingArrayJson = jsonUtil.toJson(trainingArray)
        println("Training Array em JSON: $trainingArrayJson")
        val adapter = TrainingAdapter(this, trainingArray, this)
        listView.adapter = adapter
    }
}
