package com.example.anarcomarombismo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.Adapter.ExerciseAdapter
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.google.android.material.floatingactionbutton.FloatingActionButton

class completeWorkoutActivity : AppCompatActivity() {
    private lateinit var trainingDurationTextView: TextView
    private lateinit var averageTimeTextView: TextView
    private lateinit var closeButton: FloatingActionButton
    private var trainingId: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_workout)

        // Initialize views
        trainingDurationTextView = findViewById(R.id.trainingDurationTextView)
        averageTimeTextView = findViewById(R.id.averageTimeTextView)
        closeButton = findViewById(R.id.closeFloatingButton)

        // Get trainingId from intent
        trainingId = intent.getLongExtra("trainingId", -1)

        if (trainingId != -1L) {
            displayTrainingStats(trainingId)
        }

        // Setup close button
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun displayTrainingStats(trainingId: Long) {
        val trainingDuration = ExerciseAdapter.getTimeStamp(this, trainingId)
        val formattedDuration = formatDuration(trainingDuration)
        val totalSets = DailyExercises(this).getTotalSets(trainingId)
        println("Total sets: $totalSets")
        val averageTime = calculateAverageExerciseTime(trainingDuration, totalSets)
        trainingDurationTextView.text = "${getString(R.string.training_duration)} $formattedDuration"
        averageTimeTextView.text = "${getString(R.string.average_time_per_exercise)} $averageTime"
    }

    private fun formatDuration(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds,0)
    }

    private fun calculateAverageExerciseTime(totalTime: Long, exerciseCount: Int): String {
        if (exerciseCount == 0) return "00:00:00:00"
        val averageMillis = totalTime / exerciseCount
        val hours = averageMillis / 3600000
        val minutes = (averageMillis % 3600000) / 60000
        val seconds = (averageMillis % 60000) / 1000
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, 0)
    }
}