package com.example.anarcomarombismo.Forms

import com.example.anarcomarombismo.Controller.Training
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.mainActivity

class formTraining : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var description: EditText
    private lateinit var save: Button
    private lateinit var removeTraining: Button
    private var trainingID: Long = 0
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300 // Time interval for double click detection in milliseconds
    private var lastClickTime: Long = 0
    private fun initializeUIComponents() {
        name = findViewById(R.id.name)
        description = findViewById(R.id.description)
        save = findViewById(R.id.save)
        removeTraining = findViewById(R.id.removeTraining)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_training)
        initializeUIComponents()
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
        Training().fetchById(this, trainingID).also {
            name.setText(it.name)
            description.setText(it.description)
        }

        if (trainingID > 0) {
            save.text = getString(R.string.update_training)
        }
    }

    private fun saveTraining() {
        if(buildTraining(trainingID).save(this)) {
            finish()
        }
    }

    private fun buildTraining(trainingID: Long): Training {
        return Training.build(
            trainingID = trainingID,
            name = name.text.toString().takeIf { it.isNotEmpty() } ?: getString(R.string.training),
            description = description.text.toString()
        )
    }

    private fun removeTraining() {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            if (buildTraining(trainingID).remove(this)) {
                startActivity(Intent(this, mainActivity::class.java))
            }
        } else {
            Toast.makeText(this,
                getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
        }
        lastClickTime = clickTime
    }
}
