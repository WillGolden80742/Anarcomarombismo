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
        if (BasalMetabolicRate().hasBasalMetabolicRate(this)) {
            BasalMetabolicRate().fetch(this).let {
                editTextWeight.setText(it!!.weight.toString())
                editTextHeight.setText(it!!.height.toString())
                editTextAge.setText(it!!.age.toString())
                when (it.gender) {
                    "M" -> {
                        spinnerGender.setSelection(1)
                    }
                    "F" -> {
                        spinnerGender.setSelection(2)
                    }

                }
                when (it.activityLevel) {
                    1.2 -> {
                        spinnerActivityLevel.setSelection(1)
                    }
                    1.375 -> {
                        spinnerActivityLevel.setSelection(2)
                    }
                    1.55 -> {
                        spinnerActivityLevel.setSelection(3)
                    }
                    1.725 -> {
                        spinnerActivityLevel.setSelection(4)
                    }
                    1.9 -> {
                        spinnerActivityLevel.setSelection(5)
                    }
                }

            }
        }
    }
    private fun saveBMRProfile() {
        val weightInput = editTextWeight.text.toString()
        if (weightInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_weight), Toast.LENGTH_SHORT).show()
            return
        }
        val weight = weightInput.toDoubleOrNull() ?: run {
            Toast.makeText(this, getString(R.string.invalid_weight), Toast.LENGTH_SHORT).show()
            return
        }

        val heightInput = editTextHeight.text.toString()
        if (heightInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_height), Toast.LENGTH_SHORT).show()
            return
        }
        val height = heightInput.toIntOrNull() ?: run {
            Toast.makeText(this, getString(R.string.height_invalid), Toast.LENGTH_SHORT).show()
            return
        }

        val ageInput = editTextAge.text.toString()
        if (ageInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_age), Toast.LENGTH_SHORT).show()
            return
        }
        val age = ageInput.toIntOrNull() ?: run {
            Toast.makeText(this, getString(R.string.invalid_age), Toast.LENGTH_SHORT).show()
            return
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
            weight = weight,
            height = height,
            age = age,
            gender = genderMultiplier,
            activityLevel = activityLevel
        )

        if (bmr.save(this)) {
            finish()
        }
    }

}
