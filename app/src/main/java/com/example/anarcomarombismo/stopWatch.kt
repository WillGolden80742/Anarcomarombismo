package com.example.anarcomarombismo

import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class stopWatch : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var closeButton: FloatingActionButton
    private var isRunning = false
    private var elapsedTime = 0L
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_watch)


        timerTextView = findViewById(R.id.label_timer)
        closeButton = findViewById(R.id.closeFloatingButton)

        closeButton.setOnClickListener {
            finish() // Fecha a Activity
        }
    }

    override fun onResume() {
        super.onResume()
        startStopwatch()
    }

    private fun startStopwatch() {
        if (isRunning) return
        isRunning = true
        val startTime = SystemClock.elapsedRealtime() - elapsedTime

        job = CoroutineScope(Dispatchers.Main).launch {
            while (isRunning) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime
                updateTimerText(elapsedTime)
                delay(10)
            }
        }
    }

    private fun updateTimerText(elapsedMillis: Long) {
        val hours = elapsedMillis / 3600000
        val minutes = (elapsedMillis % 3600000) / 60000
        val seconds = (elapsedMillis % 60000) / 1000
        val millis = (elapsedMillis % 1000) / 10 // Exibe os centésimos de segundo

        timerTextView.text = String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, millis)
    }
}
