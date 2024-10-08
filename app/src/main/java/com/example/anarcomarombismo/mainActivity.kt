package com.example.anarcomarombismo
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.anarcomarombismo.Controller.Adapter.TrainingAdapter
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.Forms.formTraining

class mainActivity : AppCompatActivity(),  TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: Button
    private lateinit var trainingList: ListView
    private lateinit var dailyCaloriesButton: Button
    private val listView: ListView by lazy { findViewById(R.id.trainingList) }
    private fun initializeUIComponents() {
        addTrainingButton = findViewById(R.id.addTrainingButton)
        dailyCaloriesButton = findViewById(R.id.dailyCaloriesButton)
        trainingList = findViewById(R.id.trainingList)
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initializeUIComponents()
        addTrainingButton.setOnClickListener {
            callTraining()
        }
        dailyCaloriesButton.setOnClickListener {
            callDailyCalories()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTraining()
    }

    private fun callDailyCalories() {
        try {
            startActivity(Intent(this, dailyCalories::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    override fun onItemClick(training: Training) {
        // click
    }

    private fun callTraining() {
        try {
            startActivity(Intent(this, formTraining::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de adicionar treino: $e")
        }
    }

    private fun loadTraining() {
        listView.adapter = TrainingAdapter(
            this,
            Training().fetchAll(this),
            this
        )
    }

}
