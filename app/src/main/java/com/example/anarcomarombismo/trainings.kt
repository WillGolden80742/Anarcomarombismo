package com.example.anarcomarombismo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.Adapter.TrainingAdapter
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.Forms.formTraining
import com.google.android.material.floatingactionbutton.FloatingActionButton

class trainings : AppCompatActivity(), TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: FloatingActionButton
    private lateinit var trainingList: ListView
    private lateinit var exportTrainings: FloatingActionButton
    private val listView: ListView by lazy { findViewById(R.id.trainingList) }

    private fun initializeUIComponents() {
        addTrainingButton = findViewById(R.id.addTrainingButton)
        trainingList = findViewById(R.id.trainingList)
        exportTrainings = findViewById(R.id.exportTrainings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.training_activity)
        initializeUIComponents()


        addTrainingButton.setOnClickListener {
            callTraining()
        }
        exportTrainings.setOnClickListener {
            Training.export(this)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTraining()
    }

    override fun onItemClick(training: Training) {
        // Handle item click
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
