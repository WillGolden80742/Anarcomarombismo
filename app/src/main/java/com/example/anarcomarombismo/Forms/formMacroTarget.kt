package com.example.anarcomarombismo.Forms

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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
                val weight = basalMetabolicRate.weight

                calculateAndDisplayMacros(bmr, weight)
            } else {
                Toast.makeText(this,getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
            }
        }
        editTextCalories.addTextChangedListener {
            val basalMetabolicRate = BasalMetabolicRate()
            if (basalMetabolicRate.hasBasalMetabolicRate(this)) {
                val bmr = basalMetabolicRate.fetch(this)!!.getBasalMetabolicRate()
                if (editTextCalories.text.toString().toDoubleOrNull()?:0.0 > bmr) {
                    editTextCalories.setTextColor(getColor(R.color.red))
                } else {
                    editTextCalories.setTextColor(getColor(R.color.text_primary))
                }
            }
        }
        setupCaloriesCalculation()
    }

    override fun onResume() {
        super.onResume()
        loadMacroTarget()
    }

    private fun calculateAndDisplayMacros(bmr: Double, weight: Double) {
        val proteins = calculateProteins(weight)
        val lipids = calculateLipids(bmr)
        val carbs = calculateCarbs(bmr, proteins, lipids)
        val dietaryFiber = bmr * 0.01
        updateFields(bmr, proteins, lipids, carbs,dietaryFiber)
        Toast.makeText(this, getString(R.string.basal_metabolism_calculated_successfully), Toast.LENGTH_SHORT).show()
    }

    private fun calculateProteins(weight: Double): Double {
        val proteinPerKg = 2.2
        return weight * proteinPerKg
    }


    private fun calculateLipids(bmr: Double): Double {
        val lipidPercentage = 0.30
        val caloriesPerGramOfFat = 9.0
        return (bmr * lipidPercentage) / caloriesPerGramOfFat
    }

    private fun calculateCarbs(bmr: Double, proteins: Double, lipids: Double): Double {
        val remainingCalories = bmr - (proteins * 4 + lipids * 9)
        return remainingCalories / 4
    }

    private fun updateFields(bmr: Double, proteins: Double, lipids: Double, carbs: Double,dietaryFiber:Double) {
        val decimalFormat = DecimalFormat("#.##")
        editTextCalories.setText(bmr.toString())
        editTextCarbs.setText(decimalFormat.format(carbs).replace(",", "."))
        editTextFats.setText(decimalFormat.format(lipids).replace(",", "."))
        editTextProteins.setText(decimalFormat.format(proteins).replace(",", "."))
        editTextDietaryFiber.setText(decimalFormat.format(dietaryFiber).replace(",", "."))
    }

    private fun setupCaloriesCalculation() {
        calcCalories(listOf(editTextCarbs, editTextFats, editTextProteins))
    }
    private fun calcCalories(editText:List<EditText>) {
        editText.forEach { e ->
            e.addTextChangedListener {
                val carbohydrate = editTextCarbs.text.toString()
                val lipids = editTextFats.text.toString()
                val protein = editTextProteins.text.toString()
                if (protein.isNotEmpty() && carbohydrate.isNotEmpty() && lipids.isNotEmpty()) {
                    val proteinValue = protein.toDouble()
                    val carbohydrateValue = carbohydrate.toDouble()
                    val lipidsValue = lipids.toDouble()
                    val calories = (proteinValue * 4 + carbohydrateValue * 4 + lipidsValue * 9)
                    editTextCalories.setText(formatDoubleNumber(calories))
                }
            }
        }
    }

    private fun formatDoubleNumber(value: Double):String {
        return "%.0f".format(value).replace(",", ".")
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
