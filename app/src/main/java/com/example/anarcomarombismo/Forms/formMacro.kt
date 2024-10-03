package com.example.anarcomarombismo.Forms

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.BasalMetabolicRate
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Macro
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.R
import java.text.DecimalFormat

class formMacro : AppCompatActivity() {

    private lateinit var editTextCalories: EditText
    private lateinit var editTextCarbs: EditText
    private lateinit var editTextFats: EditText
    private lateinit var editTextLipidsPerKg: EditText
    private lateinit var editTextProteins: EditText
    private lateinit var editTextProteinsPerKg: EditText
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
    private var isUpdatingCalories = false
    private var isUpdatingFats = false
    private var isUpdatingFatsPerKg = false
    private var isUpdatingProteins = false
    private var isUpdatingProteinsPerKg = false
    private var hasMetabolicRate = false
    private var isInitActivity = false
    private var basalMetabolicRate = BasalMetabolicRate()
    private var macro = Macro()
    private var minimumCalorieValue:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_macros)
        editTextCalories = findViewById(R.id.editTextCalories)
        editTextCarbs = findViewById(R.id.editTextCarbs)
        editTextFats = findViewById(R.id.editTextFats)
        editTextLipidsPerKg = findViewById(R.id.editTextLipidsPerKg)
        editTextProteins = findViewById(R.id.editTextProteins)
        editTextProteinsPerKg = findViewById(R.id.editTextProteinsPerKg)
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
            if (hasMetabolicRate) {
                val bmrValue = basalMetabolicRate?.getBasalMetabolicRate() ?: 0.0
                val weight = basalMetabolicRate?.weight
                editTextLipidsPerKg.setText(Macro().lipidsByWeight.toString())
                editTextProteinsPerKg.setText(Macro().proteinByWeight.toString())
                calculateAndDisplayMacros(bmrValue, weight?:0.0)
            } else {
                Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
            }
        }
        editTextCalories.addTextChangedListener {
            val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?:0.0
            val bmr = basalMetabolicRate.fetch(this)!!.getBasalMetabolicRate()
            if (caloriesInput > bmr+1) editTextCalories.setTextColor(getColor(R.color.red)) else editTextCalories.setTextColor(getColor(R.color.text_primary))
            if (!isUpdatingCalories) {
                val weight = basalMetabolicRate.weight
                calculateAndDisplayMacros(caloriesInput, weight,false)
            }
        }
        editTextCalories.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val isNotUpdatingFields = !isUpdatingFats && !isUpdatingProteins
                if (isNotUpdatingFields) {
                    s?.let {
                        val inputValue = it.toString().toIntOrNull()
                        if (inputValue != null && inputValue < minimumCalorieValue) {
                            isUpdatingCalories = true
                            Toast.makeText(
                                this@formMacro,
                                getString(R.string.decrease_the_value_of_protein_in_or_fats),
                                Toast.LENGTH_SHORT
                            ).show()
                            editTextCalories.setText(formatDoubleNumber(minimumCalorieValue+1))
                            isUpdatingCalories = false
                        }
                    }
                }
            }
        })
        editTextLipidsPerKg.addTextChangedListener {
            val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?: return@addTextChangedListener
            if (hasMetabolicRate && !isUpdatingFats && !isUpdatingCalories) {
                val weight = basalMetabolicRate.weight
                isUpdatingFatsPerKg = true
                calculateAndDisplayMacros(caloriesInput, weight,false)
                isUpdatingFatsPerKg = false
            } else if (!hasMetabolicRate && isInitActivity) {
                Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
            }
        }
        editTextProteinsPerKg.addTextChangedListener {
            val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?: return@addTextChangedListener
            if (hasMetabolicRate && !isUpdatingProteins && !isUpdatingCalories) {
                val weight = basalMetabolicRate.weight
                isUpdatingProteinsPerKg = true
                calculateAndDisplayMacros(caloriesInput, weight,false)
                isUpdatingProteinsPerKg = false
            } else if (!hasMetabolicRate && isInitActivity) {
                Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
            }
        }
        editTextFats.addTextChangedListener {
            isUpdatingFats = true
            val lipids = editTextFats.text.toString().toDoubleOrNull() ?: 0.0
            val proteins = editTextProteins.text.toString().toDoubleOrNull() ?: 0.0
            var calories = editTextCalories.text.toString().toDoubleOrNull() ?: 0.0
            if (hasMetabolicRate && !isUpdatingFatsPerKg) {
                calories = basalMetabolicRate.getBasalMetabolicRate()
                val weight = basalMetabolicRate.weight
                val lipidsPerKg = lipids / weight
                editTextLipidsPerKg.setText(formatDoubleNumber(lipidsPerKg, 2))
            }
            if (!isUpdatingCalories) {
                editTextCarbs.setText(
                    formatDoubleNumber(
                        calculateCarbs(calories, proteins, lipids),
                        2
                    )
                )
            }
            minimumCalorieValue = proteins * 4 + lipids * 9
            isUpdatingFats = false
        }
        editTextProteins.addTextChangedListener {
            isUpdatingProteins = true
            val proteins = editTextProteins.text.toString().toDoubleOrNull() ?: 0.0
            var calories = editTextCalories.text.toString().toDoubleOrNull() ?: 0.0
            val lipids = editTextFats.text.toString().toDoubleOrNull() ?: 0.0
            if (hasMetabolicRate && !isUpdatingProteinsPerKg) {
                calories = basalMetabolicRate.getBasalMetabolicRate()
                val weight = basalMetabolicRate.weight
                val proteinPerKg = proteins/weight
                editTextProteinsPerKg.setText(formatDoubleNumber(proteinPerKg,2))
            }
            if (!isUpdatingCalories) {
                editTextCarbs.setText(
                    formatDoubleNumber(
                        calculateCarbs(calories, proteins, lipids),
                        2
                    )
                )
            }
            minimumCalorieValue = proteins * 4 + lipids * 9
            isUpdatingProteins = false
        }
    }

    override fun onResume() {
        super.onResume()
        basalMetabolicRate = basalMetabolicRate.fetch(this)!!
        hasMetabolicRate = basalMetabolicRate.hasBasalMetabolicRate(this)
        editTextLipidsPerKg.isEnabled = hasMetabolicRate
        editTextProteinsPerKg.isEnabled = hasMetabolicRate
        val cache = Cache()
        if (!hasMetabolicRate) {
            if (!cache.hasCache(this,"accessedFormBMR")) {
                val intent = Intent(this, formBMR::class.java)
                startActivity(intent)
                Toast.makeText(
                    this,
                    getString(R.string.define_your_metabolic_profile_first),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        fetchMacroTarget()
        setupCaloriesCalculation(listOf(editTextCarbs, editTextFats,editTextLipidsPerKg))
        isInitActivity=true
    }

    private fun calculateAndDisplayMacros(bmr: Double, weight: Double,updateCalories: Boolean = true) {
        val proteins = calculateProteins(weight)
        val lipids = calculateLipids(weight)
        val carbs = calculateCarbs(bmr, proteins, lipids)
        val dietaryFiber = calculateDietaryFiber(bmr)
        updateFields(bmr, proteins, lipids, carbs, dietaryFiber,updateCalories)
        if (updateCalories) {
            Toast.makeText(
                this,
                getString(R.string.basal_metabolism_calculated_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun calculateProteins(weight: Double): Double {
        val proteinPerKg = editTextProteinsPerKg.text.toString().toDoubleOrNull() ?: 0.0
        return (weight * proteinPerKg)
    }

    private fun calculateLipids(weight: Double): Double {
        val lipidsPerKg = editTextLipidsPerKg.text.toString().toDoubleOrNull() ?: 0.0
        return weight * lipidsPerKg
    }

    private fun calculateCarbs(bmr: Double, proteins: Double, lipids: Double): Double {
        val remainingCalories = bmr - (proteins * 4 + lipids * 9)
        return if (remainingCalories <= 0) {
            isUpdatingCalories = true
            editTextCalories.setText(formatDoubleNumber(proteins * 4 + lipids * 9 + 4, 2))
            isUpdatingCalories = false
            0.0
        } else remainingCalories / 4
    }


    private fun calculateDietaryFiber(bmr: Double): Double {
        return bmr * 0.01
    }

    private fun updateFields(bmr: Double, proteins: Double, lipids: Double, carbs: Double, dietaryFiber: Double,updateFields:Boolean) {
        val decimalFormat = DecimalFormat("#.##")
        isUpdatingCalories = true
        if (updateFields) {
            editTextCalories.setText(bmr.toString())
        }
        editTextCarbs.setText(decimalFormat.format(carbs).replace(",", "."))
        editTextFats.setText(decimalFormat.format(lipids).replace(",", "."))
        editTextProteins.setText(decimalFormat.format(proteins).replace(",", "."))
        editTextDietaryFiber.setText(decimalFormat.format(dietaryFiber).replace(",", "."))
        isUpdatingCalories = false
    }


    private fun setupCaloriesCalculation(editText: List<EditText>) {
        editText.forEach { e ->
            e.addTextChangedListener {
                if (!isUpdatingCalories) {
                    val carbohydrate = editTextCarbs.text.toString()
                    val lipids = editTextFats.text.toString()
                    val protein = editTextProteins.text.toString()
                    if (protein.isNotEmpty() && carbohydrate.isNotEmpty() && lipids.isNotEmpty()) {
                        val proteinValue = protein.toDouble()
                        val carbohydrateValue = carbohydrate.toDouble()
                        val lipidsValue = lipids.toDouble()
                        val calories = (proteinValue * 4 + carbohydrateValue * 4 + lipidsValue * 9)
                        isUpdatingCalories = true
                        isUpdatingFats = true
                        isUpdatingProteins = true
                        editTextCalories.setText(formatDoubleNumber(calories))
                        isUpdatingCalories = false
                        isUpdatingFats = false
                        isUpdatingProteins = false
                    }
                }
            }
        }
    }

    private fun formatDoubleNumber(value: Double,numDecimalPlaces: Int = 0): String {
        return "%.${numDecimalPlaces}f".format(value).replace(",", ".")
    }

    private fun fetchMacroTarget() {
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
        macro.fetch(this)?.let {
            editTextCalories.setText(it.calories.toString())
            editTextCarbs.setText(it.carbs.toString())
            editTextFats.setText(it.lipids.toString())
            editTextLipidsPerKg.setText(it.lipidsByWeight.toString())
            editTextProteins.setText(it.protein.toString())
            editTextProteinsPerKg.setText(it.proteinByWeight.toString())
            editTextDietaryFiber.setText(it.dietaryFiber.toString())
        }
    }

    private fun saveMacroTarget() {
        val calories = editTextCalories.text.toString().toDoubleOrNull() ?: Macro().calories
        val carbs = editTextCarbs.text.toString().toDoubleOrNull() ?: Macro().carbs
        val lipids = editTextFats.text.toString().toDoubleOrNull() ?: Macro().lipids
        val lipidsPerKg = editTextLipidsPerKg.text.toString().toDoubleOrNull() ?: Macro().lipidsByWeight
        val protein = editTextProteins.text.toString().toDoubleOrNull() ?: Macro().protein
        val proteinPerKg = editTextProteinsPerKg.text.toString().toDoubleOrNull() ?: Macro().proteinByWeight
        val dietaryFiber = editTextDietaryFiber.text.toString().toDoubleOrNull() ?: Macro().dietaryFiber
        // class Macro ( var calories: Double = 2000.0, var lipids: Double = 44.44, val lipidsByWeight: Double = 1.0, var carbs: Double = 250.0, val protein: Double = 150.0, val proteinByWeight: Double = 2.0, val dietaryFiber: Double = 20.0, )
        if (Macro.build(
                calories,
                lipids,
                lipidsPerKg,
                carbs,
                protein,
                proteinPerKg,
                dietaryFiber
            ).save(this)) {
            finish()
        }
    }
}