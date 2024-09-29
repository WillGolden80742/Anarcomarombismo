package com.example.anarcomarombismo.Forms

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.MacroTarget
import com.example.anarcomarombismo.R

class formMacros : AppCompatActivity() {

    // Atributos para os campos de entrada e barras de progresso
    private lateinit var editTextCalories: EditText
    private lateinit var editTextCarbs: EditText
    private lateinit var editTextFats: EditText
    private lateinit var editTextProteins: EditText
    private lateinit var caloriesProgressBar: ProgressBar
    private lateinit var carbsProgressBar: ProgressBar
    private lateinit var fatsProgressBar: ProgressBar
    private lateinit var proteinsProgressBar: ProgressBar
    private lateinit var saveTargetButton: Button
    private lateinit var caloriesLabel: TextView
    private lateinit var carbsLabel: TextView
    private lateinit var lipidsLabel: TextView
    private lateinit var proteinsLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_macros)

        // Inicializa os campos de entrada e barras de progresso
        editTextCalories = findViewById(R.id.editTextCalories)
        editTextCarbs = findViewById(R.id.editTextCarbs)
        editTextFats = findViewById(R.id.editTextFats)
        editTextProteins = findViewById(R.id.editTextProteins)
        caloriesProgressBar = findViewById(R.id.caloriesProgressBar)
        carbsProgressBar = findViewById(R.id.carbsProgressBar)
        fatsProgressBar = findViewById(R.id.fatsProgressBar)
        proteinsProgressBar = findViewById(R.id.proteinsProgressBar)
        saveTargetButton = findViewById(R.id.saveTargetButton)
        caloriesLabel = findViewById(R.id.caloriesLabel)
        carbsLabel = findViewById(R.id.carbsLabel)
        lipidsLabel = findViewById(R.id.lipidsLabel)
        proteinsLabel = findViewById(R.id.proteinsLabel)

        saveTargetButton.setOnClickListener {
            saveMacroTarget()
        }
    }

    override fun onResume() {
        super.onResume()
        loadMacroTarget()
    }

    private fun loadMacroTarget() {
        DailyCalories().loadMacroTarget(
            this,
            caloriesProgressBar,
            carbsProgressBar,
            fatsProgressBar,
            proteinsProgressBar,
            caloriesLabel,
            carbsLabel,
            lipidsLabel,
            proteinsLabel
        )
        MacroTarget().fetchById(this)?.let {
            editTextCalories.setText(it.calories.toString())
            editTextCarbs.setText(it.carbs.toString())
            editTextFats.setText(it.lipids.toString())
            editTextProteins.setText(it.protein.toString())
        }
    }

    private fun saveMacroTarget() {
        val calories = editTextCalories.text.toString().toDoubleOrNull() ?: MacroTarget().calories
        val carbs = editTextCarbs.text.toString().toDoubleOrNull() ?: MacroTarget().carbs
        val lipids = editTextFats.text.toString().toDoubleOrNull() ?: MacroTarget().lipids
        val protein = editTextProteins.text.toString().toDoubleOrNull() ?: MacroTarget().protein

        if (MacroTarget.build(
                calories = calories,
                lipids = lipids,
                carbs = carbs,
                protein = protein
        ).save(this)) {
            finish()
        }
    }

}
