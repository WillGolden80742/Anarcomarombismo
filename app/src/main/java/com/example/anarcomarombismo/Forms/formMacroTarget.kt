package com.example.anarcomarombismo.Forms

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.BasalMetabolicRate
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.MacroTarget
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCalories
import java.text.DecimalFormat

class formMacroTarget : AppCompatActivity() {

    // Atributos para os campos de entrada e barras de progresso
    private lateinit var editTextCalories: EditText
    private lateinit var editTextCarbs: EditText
    private lateinit var editTextFats: EditText
    private lateinit var editTextProteins: EditText
    private lateinit var editTextDietaryFiber: EditText
    private lateinit var caloriesProgressBar: ProgressBar
    private lateinit var carbsProgressBar: ProgressBar
    private lateinit var fatsProgressBar: ProgressBar
    private lateinit var proteinsProgressBar: ProgressBar
    private lateinit var dietaryFiberProgressBar: ProgressBar
    private lateinit var saveTargetButton: Button
    private lateinit var caloriesLabel: TextView
    private lateinit var carbsLabel: TextView
    private lateinit var lipidsLabel: TextView
    private lateinit var proteinsLabel: TextView
    private lateinit var dietaryFiberLabel: TextView
    private lateinit var calculateBasalButton: Button
    private lateinit var editBasalButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_macros)

        // Inicializa os campos de entrada e barras de progresso
        editTextCalories = findViewById(R.id.editTextCalories)
        editTextCarbs = findViewById(R.id.editTextCarbs)
        editTextFats = findViewById(R.id.editTextFats)
        editTextProteins = findViewById(R.id.editTextProteins)
        editTextDietaryFiber = findViewById(R.id.editTextDietaryFiber)
        caloriesProgressBar = findViewById(R.id.caloriesProgressBar)
        carbsProgressBar = findViewById(R.id.carbsProgressBar)
        fatsProgressBar = findViewById(R.id.fatsProgressBar)
        proteinsProgressBar = findViewById(R.id.proteinsProgressBar)
        dietaryFiberProgressBar = findViewById(R.id.dietaryFiberProgressBar)
        saveTargetButton = findViewById(R.id.saveTargetButton)
        caloriesLabel = findViewById(R.id.caloriesLabel)
        carbsLabel = findViewById(R.id.carbsLabel)
        lipidsLabel = findViewById(R.id.lipidsLabel)
        proteinsLabel = findViewById(R.id.proteinsLabel)
        dietaryFiberLabel = findViewById(R.id.dietaryFiberLabel)
        calculateBasalButton = findViewById(R.id.calculateBasalButton)
        editBasalButton = findViewById(R.id.editBasalButton)
        saveTargetButton.setOnClickListener {
            saveMacroTarget()
        }
        editBasalButton.setOnClickListener {
            val intent = Intent(this, formBMR::class.java)
            startActivity(intent)
        }
        calculateBasalButton.setOnClickListener {
            val basalMetabolicRate = BasalMetabolicRate()

            if (basalMetabolicRate.hasBasalMetabolicRate(this)) {
                val bmr = basalMetabolicRate.fetch(this)!!.getBasalMetabolicRate()
                val decimalFormat = DecimalFormat("#.##")

                editTextCalories.setText(bmr.toString())

                val bmrCarbs = decimalFormat.format(bmr * 0.125).replace(",", ".")
                val bmrFats = decimalFormat.format(bmr * 0.02222).replace(",", ".")
                val bmrProteins = decimalFormat.format(bmr * 0.075).replace(",", ".")
                val bmrDietaryFiber = decimalFormat.format(bmr * 0.020).replace(",", ".")

                editTextCarbs.setText(bmrCarbs)
                editTextFats.setText(bmrFats)
                editTextProteins.setText(bmrProteins)
                editTextDietaryFiber.setText(bmrDietaryFiber)
                Toast.makeText(this,
                    getString(R.string.basal_metabolism_calculated_successfully), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
            }
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
            dietaryFiberProgressBar,
            caloriesLabel,
            carbsLabel,
            lipidsLabel,
            proteinsLabel,
            dietaryFiberLabel
        )
        MacroTarget().fetch(this)?.let {
            editTextCalories.setText(it.calories.toString())
            editTextCarbs.setText(it.carbs.toString())
            editTextFats.setText(it.lipids.toString())
            editTextProteins.setText(it.protein.toString())
            editTextDietaryFiber.setText(it.dietaryFiber.toString())
        }
    }

    private fun saveMacroTarget() {
        val calories = editTextCalories.text.toString().toDoubleOrNull() ?: MacroTarget().calories
        val carbs = editTextCarbs.text.toString().toDoubleOrNull() ?: MacroTarget().carbs
        val lipids = editTextFats.text.toString().toDoubleOrNull() ?: MacroTarget().lipids
        val protein = editTextProteins.text.toString().toDoubleOrNull() ?: MacroTarget().protein
        val dietaryFiber = editTextDietaryFiber.text.toString().toDoubleOrNull() ?: MacroTarget().dietaryFiber
        if (MacroTarget.build(
                calories = calories,
                lipids = lipids,
                carbs = carbs,
                protein = protein,
                dietaryFiber = dietaryFiber
        ).save(this)) {
            val intent = Intent(this,dailyCalories::class.java)
            startActivity(intent)
        }
    }

}
