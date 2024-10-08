package com.example.anarcomarombismo.Forms

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.BasalMetabolicRate
import com.example.anarcomarombismo.Controller.Macro
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.NumberFormatter
import com.example.anarcomarombismo.R
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat

class formMacro : AppCompatActivity() {

    private lateinit var editTextCalories: EditText
    private lateinit var updateMetaCheckbox: CheckBox
    private lateinit var editTextCarbs: EditText
    private lateinit var editTextLipids: EditText
    private lateinit var editTextLipidsByWeight: EditText
    private lateinit var textInputLayoutLipidsPerKg: TextInputLayout
    private lateinit var editTextProteins: EditText
    private lateinit var editTextProteinsByWeight: EditText
    private lateinit var textInputLayoutProteinsPerKg: TextInputLayout
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
    private var isUpdatingMetaCheckbox = false
    private var hasMetabolicRate = false
    private var isInitActivity = false
    private var basalMetabolicRate = BasalMetabolicRate()
    private var macro = Macro()
    private var minimumCalorieValue:Double = 0.0
    private var updateMetaCalories:Double = 0.0
    private var isBackspacePressed = false
    private var indexCursor = 0
    companion object {
        private const val proteinCals = 4
        private const val carbsCals = 4
        private const val lipidsCals = 9
    }
    private fun initializeUIComponents() {
        editTextCalories = findViewById(R.id.editTextCalories)
        updateMetaCheckbox = findViewById(R.id.updateMetaCheckbox)
        editTextCarbs = findViewById(R.id.editTextCarbs)
        editTextLipids = findViewById(R.id.editTextLipids)
        editTextLipidsByWeight = findViewById(R.id.editTextLipidsByWeight)
        textInputLayoutLipidsPerKg = findViewById(R.id.textInputLayoutLipidsPerKg)
        editTextProteins = findViewById(R.id.editTextProteins)
        editTextProteinsByWeight = findViewById(R.id.editTextProteinsByWeight)
        textInputLayoutProteinsPerKg = findViewById(R.id.textInputLayoutProteinsPerKg)
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
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_macros)
        initializeUIComponents()
        saveTargetButton.setOnClickListener {
            saveAndUpdateMacroUI()
        }
        editBasalButton.setOnClickListener {
            val intent = Intent(this, formBMR::class.java)
            startActivity(intent)
        }
        calculateBasalButton.setOnClickListener {
            handleBasalCalculation()
        }
        updateMetaCheckbox.setOnCheckedChangeListener { _, _ ->
            if (!isUpdatingMetaCheckbox) {
                updateMetaCalories = editTextCalories.text.toString().toDoubleOrNull() ?: 0.0
                updateMetaCheckbox.isVisible = false
                saveAndUpdateMacroUI()
            }
        }
        editTextCalories.addTextChangedListener(object : TextWatcher {
            private var previousTextLength = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isBackspacePressed = (previousTextLength > (s?.length ?: 0))
            }
            override fun afterTextChanged(s: Editable?) {
                handleCaloriesInput(s)
            }
        })
        editTextLipidsByWeight.addTextChangedListener { onLipidsPerKgTextChanged() }
        editTextProteinsByWeight.addTextChangedListener { onProteinsPerKgTextChanged() }
        editTextLipids.addTextChangedListener(createTextWatcherForFats())
        editTextProteins.addTextChangedListener(createTextWatcherForProteins())
    }

    override fun onResume() {
        super.onResume()
        basalMetabolicRate = basalMetabolicRate.fetch(this)!!
        hasMetabolicRate = basalMetabolicRate.hasBasalMetabolicRate(this)
        textInputLayoutLipidsPerKg.isVisible = hasMetabolicRate
        textInputLayoutProteinsPerKg.isVisible = hasMetabolicRate
        if (!hasMetabolicRate && !isAccessedFormBMR()) {
            val intent = Intent(this, formBMR::class.java)
            startActivity(intent)
            showMetabolicProfileRequiredMessage()
        }
        loadAndUpdateMacroUI()
        setupCaloriesCalculation(listOf(editTextCarbs, editTextLipids,editTextLipidsByWeight))
        isInitActivity=true
    }
    private fun isAccessedFormBMR(): Boolean {
        return Cache().hasCache(this,"accessedFormBMR")
    }
    private fun handleBasalCalculation() {
        if (!hasMetabolicRate) {
            showMetabolicProfileRequiredMessage()
            return
        }
        updateBasalValues()
        performBasalCalculation()
        updateMetaCheckboxState()
    }

    private fun showMetabolicProfileRequiredMessage() {
        Toast.makeText(
            this,
            getString(R.string.define_your_metabolic_profile_first),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateBasalValues() {
        val defaultMacro = Macro()
        editTextLipidsByWeight.setText(defaultMacro.lipidsByWeight.toString())
        editTextProteinsByWeight.setText(defaultMacro.proteinByWeight.toString())
    }

    private fun performBasalCalculation() {
        val bmrValue = basalMetabolicRate.getBasalMetabolicRate()
        val weight = basalMetabolicRate.weight ?: 0.0
        calculateAndDisplayMacros(bmrValue, weight)
    }

    private fun updateMetaCheckboxState() {
        updateMetaCheckbox.apply {
            isVisible = true
            isUpdatingMetaCheckbox = true
            isChecked = false
            isUpdatingMetaCheckbox = false
        }
    }

    private fun handleCaloriesInput(s: Editable?) {
        var caloriesInput = s.toString().toDoubleOrNull() ?: 0.0
        updateCaloriesColor()
        if (!isUpdatingCalories && isInitActivity) {
            val currentLength = NumberFormatter.formatDoubleNumber(caloriesInput).length
            val currentCursor = editTextCalories.selectionStart
            if (currentLength > 5) {
                isUpdatingCalories = true
                indexCursor = if (isBackspacePressed) {
                    maxOf(currentCursor, 0)
                } else {
                    caloriesInput = (s.toString().toDoubleOrNull() ?: 0.0) / (10)
                    minOf(currentCursor, currentLength-1)
                }
                editTextCalories.setText(NumberFormatter.formatDoubleNumber(caloriesInput))
                editTextCalories.setSelection(indexCursor)
                isUpdatingCalories = false
            } else {
                indexCursor = currentCursor
            }
            updateMetaCheckbox.apply {
                isVisible = true
                isUpdatingMetaCheckbox = true
                isChecked = false
                isUpdatingMetaCheckbox = false
            }
        }
        if (!isUpdatingCalories) {
            val weight = basalMetabolicRate.weight
            calculateAndDisplayMacros(caloriesInput, weight, false)
        }
        validateCaloriesAgainstMinimum(s)
        isBackspacePressed = false
    }

    private fun updateCaloriesColor() {
        val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?: 0.0
        val calories = Macro().fetch(this@formMacro)!!.calories
        val color = if (caloriesInput > calories + 1) {
            updateMetaCheckbox.apply {
                isVisible = true
                isUpdatingMetaCheckbox = true
                isChecked = false
                isUpdatingMetaCheckbox = false
            }
            getColor(R.color.red)
        } else {
            updateMetaCheckbox.apply {
                isVisible = false
            }
            getColor(R.color.text_primary)
        }
        editTextCalories.setTextColor(color)
    }

    private fun validateCaloriesAgainstMinimum(s: Editable?) {
        val isNotUpdatingFields = !isUpdatingFats && !isUpdatingProteins
        if (isNotUpdatingFields) {
            s?.let {
                val inputValue = it.toString().toDoubleOrNull()
                if (inputValue != null && inputValue < minimumCalorieValue) {
                    isUpdatingCalories = true
                    Toast.makeText(
                        this@formMacro,
                        getString(R.string.decrease_the_value_of_protein_in_or_fats),
                        Toast.LENGTH_SHORT
                    ).show()
                    editTextCalories.setText(NumberFormatter.formatDoubleNumber(minimumCalorieValue + 1))
                    isUpdatingCalories = false
                }
            }
        }
    }

    private fun onLipidsPerKgTextChanged() {
        val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?: return
        if (hasMetabolicRate && !isUpdatingFats && !isUpdatingCalories) {
            val weight = basalMetabolicRate.weight
            isUpdatingFatsPerKg = true
            calculateAndDisplayMacros(caloriesInput, weight, false)
            if (!isUpdatingCalories) {
                val proteins = editTextProteins.text.toString().toDoubleOrNull() ?: 0.0
                val lipids = editTextLipids.text.toString().toDoubleOrNull() ?: 0.0
                updateCarbs(proteins, lipids)
            }
            isUpdatingFatsPerKg = false
        } else if (!hasMetabolicRate && isInitActivity) {
            Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProteinsPerKgTextChanged() {
        val caloriesInput = editTextCalories.text.toString().toDoubleOrNull() ?: return
        if (hasMetabolicRate && !isUpdatingProteins && !isUpdatingCalories) {
            val weight = basalMetabolicRate.weight
            isUpdatingProteinsPerKg = true
            calculateAndDisplayMacros(caloriesInput, weight, false)
            if (!isUpdatingCalories) {
                val proteins = editTextProteins.text.toString().toDoubleOrNull() ?: 0.0
                val lipids = editTextLipids.text.toString().toDoubleOrNull() ?: 0.0
                updateCarbs(proteins, lipids)
            }
            isUpdatingProteinsPerKg = false
        } else if (!hasMetabolicRate && isInitActivity) {
            Toast.makeText(this, getString(R.string.define_your_metabolic_profile_first), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTextWatcherForFats(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingFats) return
                isUpdatingFats = true

                val lipids = s.toString().toDoubleOrNull() ?: 0.0
                val proteins = editTextProteins.text.toString().toDoubleOrNull() ?: 0.0

                if (hasMetabolicRate && !isUpdatingFatsPerKg) {
                    val weight = basalMetabolicRate.weight
                    val lipidsPerKg = lipids / weight
                    editTextLipidsByWeight.setText(NumberFormatter.formatDoubleNumber(lipidsPerKg, 2))
                }

                if (!isUpdatingCalories) {
                    updateCarbs(proteins, lipids)
                }

                minimumCalorieValue = calculateMinimumCalorieValue(proteins, lipids)
                isUpdatingFats = false
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun createTextWatcherForProteins(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingProteins) return
                isUpdatingProteins = true

                val proteins = s.toString().toDoubleOrNull() ?: 0.0
                val lipids = editTextLipids.text.toString().toDoubleOrNull() ?: 0.0

                if (hasMetabolicRate && !isUpdatingProteinsPerKg) {
                    val weight = basalMetabolicRate.weight
                    val proteinPerKg = proteins / weight
                    editTextProteinsByWeight.setText(NumberFormatter.formatDoubleNumber(proteinPerKg, 2))
                }

                if (!isUpdatingCalories) {
                    updateCarbs(proteins, lipids)
                }

                minimumCalorieValue = calculateMinimumCalorieValue(proteins, lipids)
                isUpdatingProteins = false
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }
    private fun updateCarbs(proteins: Double, lipids: Double) {
        editTextCarbs.setText(
            NumberFormatter.formatDoubleNumber(
                calculateCarbs(proteins, lipids),
                2
            )
        )
    }
    private fun calculateMinimumCalorieValue(proteins: Double, lipids: Double): Double {
        return proteins * proteinCals + lipids * lipidsCals
    }


    private fun calculateAndDisplayMacros(bmr: Double, weight: Double,updateCalories: Boolean = true) {
        val proteins = calculateProteins(weight)
        val lipids = calculateLipids(weight)
        val dietaryFiber = calculateDietaryFiber(bmr)
        val carbs = calculateCarbs(proteins, lipids,bmr)
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
        val proteinPerKg = editTextProteinsByWeight.text.toString().toDoubleOrNull() ?: 0.0
        return (weight * proteinPerKg)
    }

    private fun calculateLipids(weight: Double): Double {
        val lipidsPerKg = editTextLipidsByWeight.text.toString().toDoubleOrNull() ?: 0.0
        return weight * lipidsPerKg
    }
    private fun calculateCarbs(proteins: Double, lipids: Double,calories: Double = Macro().fetch(this@formMacro)!!.calories): Double {
        val remainingCalories = calories - (proteins * proteinCals + lipids * lipidsCals)
        return if (remainingCalories <= 0) {
            isUpdatingCalories = true
            editTextCalories.setText(
                NumberFormatter.formatDoubleNumber(
                    proteins * proteinCals + lipids * lipidsCals,
                    2
                )
            )
            isUpdatingCalories = false
            0.0
        } else remainingCalories / carbsCals
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
        editTextLipids.setText(decimalFormat.format(lipids).replace(",", "."))
        editTextProteins.setText(decimalFormat.format(proteins).replace(",", "."))
        editTextDietaryFiber.setText(decimalFormat.format(dietaryFiber).replace(",", "."))
        isUpdatingCalories = false
    }


    private fun setupCaloriesCalculation(editText: List<EditText>) {
        editText.forEach { e ->
            e.addTextChangedListener {
                if (!isUpdatingCalories) {
                    val protein = editTextProteins.text.toString()
                    val carbohydrate = editTextCarbs.text.toString()
                    val lipids = editTextLipids.text.toString()
                    if (protein.isNotEmpty() && carbohydrate.isNotEmpty() && lipids.isNotEmpty()) {
                        val proteinValue = protein.toDouble()
                        val carbohydrateValue = carbohydrate.toDouble()
                        val lipidsValue = lipids.toDouble()
                        val calories = (proteinValue * proteinCals + carbohydrateValue * carbsCals + lipidsValue * lipidsCals)
                        setIsUpdatingMacros(true)
                        editTextCalories.setText(NumberFormatter.formatDoubleNumber(calories))
                        setIsUpdatingMacros(false)
                    }
                }
            }
        }
    }

    private fun setIsUpdatingMacros (b: Boolean) {
        setIsUpdatingMacros(b,b,b)
    }
    private fun setIsUpdatingMacros(calories:Boolean,fats:Boolean,proteins:Boolean) {
        isUpdatingCalories = calories
        isUpdatingFats = fats
        isUpdatingProteins = proteins
    }

    private fun loadAndUpdateMacroUI() {
        macro.loadAndUpdateMacroUI(
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
            editTextLipids.setText(it.lipids.toString())
            editTextLipidsByWeight.setText(it.lipidsByWeight.toString())
            editTextProteins.setText(it.protein.toString())
            editTextProteinsByWeight.setText(it.proteinByWeight.toString())
            editTextDietaryFiber.setText(it.dietaryFiber.toString())
        }
    }

    private fun saveAndUpdateMacroUI() {
        Macro.build(
            calories = editTextCalories.text.toString().toDoubleOrNull() ?: Macro().calories,
            lipids = editTextLipids.text.toString().toDoubleOrNull() ?: Macro().lipids,
            lipidsByWeight =  editTextLipidsByWeight.text.toString().toDoubleOrNull() ?: Macro().lipidsByWeight,
            carbs = editTextCarbs.text.toString().toDoubleOrNull() ?: Macro().carbs,
            protein =  editTextProteins.text.toString().toDoubleOrNull() ?: Macro().protein,
            proteinByWeight = editTextProteinsByWeight.text.toString().toDoubleOrNull() ?: Macro().proteinByWeight,
            dietaryFiber = editTextDietaryFiber.text.toString().toDoubleOrNull() ?: Macro().dietaryFiber
        ).let {
            if (it.save(this)) {
                it.loadAndUpdateMacroUI(
                    context = this,
                    caloriesProgressBar = caloriesProgressBar,
                    carbsProgressBar = carbsProgressBar,
                    fatsProgressBar = fatsProgressBar,
                    proteinsProgressBar = proteinsProgressBar,
                    dietaryFiberProgressBar = dietaryFiberProgressBar,
                    caloriesLabel = caloriesLabel,
                    carbsLabel = carbsLabel,
                    fatsLabel = lipidsLabel,
                    proteinsLabel = proteinsLabel,
                    dietaryFiberLabel = dietaryFiberLabel,
                    miniVersion = false,
                    macro = it
                )
                Toast.makeText(this, getString(R.string.objective_saved), Toast.LENGTH_SHORT).show()
                updateCaloriesColor()
            }
        }
    }
}