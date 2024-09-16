package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.Training
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible

class formTraining : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var description: EditText
    private lateinit var save: Button
    private lateinit var removeTraining: Button
    private var trainingID: Long = 0
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300 // Time interval for double click detection in milliseconds
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_training)

        name = findViewById(R.id.name)
        description = findViewById(R.id.description)
        save = findViewById(R.id.save)
        removeTraining = findViewById(R.id.removeTraining)

        trainingID = intent.getLongExtra("trainingID", 0)

        save.setOnClickListener {
            saveTraining()
        }

        if (trainingID == 0L) {
            removeTraining.isVisible = false
        }

        removeTraining.setOnClickListener {
            removeTraining()
        }

        // Load training data if it exists
        Training().load(this, trainingID).also {
            name.setText(it.name)
            description.setText(it.description)
        }

        if (trainingID > 0) {
            save.text = getString(R.string.update_training)
        }
    }

    private fun saveTraining() {
        val training = Training(trainingID, name.text.toString(), description.text.toString())
        if(training.save(this)) {
            finish()
        }
    }

    private fun removeTraining() {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            val training = Training(trainingID)
            if (training.remove(this)) {
                startActivity(Intent(this, training_main::class.java))
            }
        } else {
            Toast.makeText(this,
                getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
        }
        lastClickTime = clickTime
    }
}
