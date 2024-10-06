package com.example.anarcomarombismo.Forms
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.BasalMetabolicRate
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.R

class formBMR : AppCompatActivity() {

    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var saveProfileButton: Button
    private fun initializeUIComponents () {
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextAge = findViewById(R.id.editTextAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        saveProfileButton = findViewById(R.id.addBMRFormButton)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_bmr)
        initializeUIComponents()
        saveProfileButton.setOnClickListener {
            saveBMRProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchBMRProfile()
        Cache().setCache(this,"accessedFormBMR",true)
    }

    private fun fetchBMRProfile() {
        BasalMetabolicRate().takeIf { it.hasBasalMetabolicRate(this) }?.fetch(this)?.let {
            editTextWeight.setText(it.weight.toString())
            editTextHeight.setText(it.height.toString())
            editTextAge.setText(it.age.toString())
            spinnerGender.setSelection(
                when (it.gender) {
                    "M" -> 1
                    "F" -> 2
                    else -> 0
                }
            )
            spinnerActivityLevel.setSelection(
                when (it.activityLevel) {
                    1.2 -> 1
                    1.375 -> 2
                    1.55 -> 3
                    1.725 -> 4
                    1.9 -> 5
                    else -> 0
                }
            )
        }
    }
    private fun saveBMRProfile() {
        val weight = editTextWeight.text.toString().toDoubleOrNull()
        val height = editTextHeight.text.toString().toIntOrNull()
        val age = editTextAge.text.toString().toIntOrNull()
        val gender = when (spinnerGender.selectedItemId) {
            1L -> "M"
            2L -> "F"
            else -> null
        }
        val activityLevel = when (spinnerActivityLevel.selectedItemId) {
            1L -> 1.2
            2L -> 1.375
            3L -> 1.55
            4L -> 1.725
            5L -> 1.9
            else -> null
        }
        when {
            weight == null -> showError(R.string.mandatory_weight)
            height == null -> showError(R.string.mandatory_height)
            age == null -> showError(R.string.mandatory_age)
            gender == null -> showError(R.string.mandatory_gender)
            activityLevel == null -> showError(R.string.mandatory_activity_level)
            else -> {
                if (BasalMetabolicRate.build(
                    weight = weight,
                    height = height,
                    age = age,
                    gender = gender,
                    activityLevel = activityLevel
                ).save(this)) {
                    finish()
                }
            }
        }
    }
    private fun showError(messageId: Int) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show()
    }
}
