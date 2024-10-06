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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_bmr)
        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextAge = findViewById(R.id.editTextAge)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel)
        saveProfileButton = findViewById(R.id.addBMRFormButton)
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
        val weight = editTextWeight.text.toString().toDoubleOrNull().also {
            if (it == null) {
                Toast.makeText(this, getString(R.string.mandatory_weight), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val height = editTextHeight.text.toString().toIntOrNull().also {
            if (it == null) {
                Toast.makeText(this, getString(R.string.mandatory_height), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val age = editTextAge.text.toString().toIntOrNull().also {
            if (it == null) {
                Toast.makeText(this, getString(R.string.mandatory_age), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val genderMultiplier = when (spinnerGender.selectedItemId) {
            1L -> "M"
            2L -> "F"
            else -> {
                Toast.makeText(this, getString(R.string.mandatory_gender), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val activityLevel = when (spinnerActivityLevel.selectedItemId) {
            1L -> 1.2
            2L -> 1.375
            3L -> 1.55
            4L -> 1.725
            5L -> 1.9
            else -> {
                Toast.makeText(this, getString(R.string.mandatory_activity_level), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val bmr = BasalMetabolicRate.build(
            weight = weight ?: 0.0,
            height = height ?: 0,
            age = age ?: 0,
            gender = genderMultiplier,
            activityLevel = activityLevel
        )
        if (bmr.save(this)) {
            finish()
        }
    }

}
