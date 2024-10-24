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

class mainActivity : AppCompatActivity(), TrainingAdapter.OnTrainingItemClickListener {

    private lateinit var addTrainingButton: Button
    private lateinit var trainingList: ListView
    private lateinit var importTrainings: FloatingActionButton
    private lateinit var exportTrainings: FloatingActionButton
    private lateinit var dailyCaloriesButton: Button
    private val listView: ListView by lazy { findViewById(R.id.trainingList) }

    private lateinit var launcher: ActivityResultLauncher<Array<String>> // Declare the launcher

    private fun initializeUIComponents() {
        addTrainingButton = findViewById(R.id.addTrainingButton)
        dailyCaloriesButton = findViewById(R.id.dailyCaloriesButton)
        trainingList = findViewById(R.id.trainingList)
        importTrainings = findViewById(R.id.importTrainings)
        exportTrainings = findViewById(R.id.exportTrainings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initializeUIComponents()

        launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                Training().handleImportResult(it, this)
            } ?: showToastMessage(this, false, R.string.error_no_file_selected, R.string.error_no_file_selected)
        }

        addTrainingButton.setOnClickListener {
            callTraining()
        }
        dailyCaloriesButton.setOnClickListener {
            callDailyCalories()
        }
        exportTrainings.setOnClickListener {
            Training.export(this)
        }
        importTrainings.setOnClickListener {
            launcher.launch(arrayOf("application/octet-stream"))
        }
    }

    override fun onResume() {
        super.onResume()
        handleIncomingFile(intent)
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

    private fun handleIncomingFile(intent: Intent?) {
        intent?.let { safeIntent ->
            when (safeIntent.action) {
                Intent.ACTION_VIEW -> {
                    val uri = safeIntent.data
                    if (uri != null) {
                        Training.import(this,uri)
                        safeIntent.action = null
                    }
                }
            }
        }
    }


    private fun showToastMessage(context: Context, isUpdate: Boolean, addMessageId: Int, updateMessageId: Int) {
        val messageResId = if (isUpdate) updateMessageId else addMessageId
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}
