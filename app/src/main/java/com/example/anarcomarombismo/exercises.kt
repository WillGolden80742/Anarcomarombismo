package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.Adapter.ExerciseAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Util.Calendars
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.Forms.formExercise
import com.example.anarcomarombismo.Forms.formTraining
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class exercises : AppCompatActivity(), ExerciseAdapter.OnExerciseCheckListener  {

    private lateinit var dateTextView: TextView
    private lateinit var trainingLabel: TextView
    private lateinit var trainingProgressBar: ProgressBar
    private lateinit var addExerciseButton: Button
    private lateinit var editTraining: Button
    private lateinit var exerciseList: RecyclerView
    private lateinit var trainingName: TextView
    private lateinit var descriptionTrainingLabel: TextView
    private var trainingID: Long = 0
    private val dateFormatStored = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun initializeUIComponents() {
        dateTextView = findViewById(R.id.dateTextView)
        trainingLabel = findViewById(R.id.trainingLabel)
        trainingProgressBar = findViewById(R.id.trainingProgressBar)
        addExerciseButton = findViewById(R.id.addFoodFormButton)
        exerciseList = findViewById(R.id.exercisesList)
        editTraining = findViewById(R.id.removeFoodFormButton)
        descriptionTrainingLabel = findViewById(R.id.descriptionTrainingLabel)
        trainingName = findViewById(R.id.dailyCaloriesTitle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        initializeUIComponents()
        trainingID = intent.getLongExtra("trainingID", 0)
        dateTextView.text = getCurrentDate()
        addExerciseButton.setOnClickListener {
            callAddExercise()
        }
        editTraining.setOnClickListener {
            editTraining()
        }
        dateTextView.setOnClickListener {
            selectDate()
        }

        exerciseList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(exerciseList) // anexa o SnapHelper ao RecyclerView

        exerciseList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val centerPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        ExerciseAdapter.setItemPositionIndex(this@exercises,trainingID, centerPosition)
                    }
                }
            }
        })

    }
    override fun onResume() {
        super.onResume()
        loadExercises(trainingID,dateTextView.text.toString())
        exerciseList.scrollToPosition(ExerciseAdapter.getItemPositionIndex(this,trainingID))
        updateProgressTraining()
    }
    private fun updateProgressTraining() {
        val percentageLabel = DailyExercises(this).getTrainingCompletionPercentage(trainingID)
        trainingLabel.text = "$percentageLabel%"
        val percentage = percentageLabel.toInt()
        trainingProgressBar.progress = percentage
    }
    private fun selectDate() {
        val calendar = Calendar.getInstance()
        val maxDate = calendar.timeInMillis
        Calendars.selectDate(this, dateTextView, maxDate) {
            loadExercises(trainingID, it)
        }
    }
    private fun getCurrentDate(): String {
        val currentDate = Date()
        return dateFormatStored.format(currentDate)
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

    private fun loadExercises(trainingID: Long, date: String) {
        Training().fetchById(this, trainingID).also {
            trainingName.text = it.name
            descriptionTrainingLabel.text = it.description
            exerciseList.adapter = ExerciseAdapter(this, Exercise.build(trainingID).fetchAll(this), date, exerciseList)
        }
    }

    override fun onExerciseCheckChanged() {
        updateProgressTraining()
    }


}
