package com.example.anarcomarombismo.Forms
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.BasalMetabolicRate
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
        loadBMRProfile()
    }

    private fun loadBMRProfile() {
        if (BasalMetabolicRate().hasBasalMetabolicRate(this)) {
            BasalMetabolicRate().fetch(this).let {
                editTextWeight.setText(it!!.weight.toString())
                editTextHeight.setText(it!!.height.toString())
                editTextAge.setText(it!!.age.toString())
                when (it.gender) {
                    "M" -> {
                        spinnerGender.setSelection(0)
                    }
                    "F" -> {
                        spinnerGender.setSelection(1)
                    }

                }
                when (it.activityLevel) {
                    1.2 -> {
                        spinnerActivityLevel.setSelection(0)
                    }
                    1.375 -> {
                        spinnerActivityLevel.setSelection(1)
                    }
                    1.55 -> {
                        spinnerActivityLevel.setSelection(2)
                    }
                    1.725 -> {
                        spinnerActivityLevel.setSelection(3)
                    }
                    1.9 -> {
                        spinnerActivityLevel.setSelection(4)
                    }
                }

            }
        }
    }
    private fun saveBMRProfile() {
        // Verificar se o campo de peso está preenchido
        val weightInput = editTextWeight.text.toString()
        if (weightInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_weight), Toast.LENGTH_SHORT).show()
            return
        }
        val weight = weightInput.toDoubleOrNull() ?: run {
            Toast.makeText(this, getString(R.string.invalid_weight), Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar se o campo de altura está preenchido
        val heightInput = editTextHeight.text.toString()
        if (heightInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_height), Toast.LENGTH_SHORT).show()
            return
        }
        val height = heightInput.toIntOrNull() ?: run {
            Toast.makeText(this, getString(R.string.height_invalid), Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar se o campo de idade está preenchido
        val ageInput = editTextAge.text.toString()
        if (ageInput.isEmpty()) {
            Toast.makeText(this, getString(R.string.mandatory_age), Toast.LENGTH_SHORT).show()
            return
        }
        val age = ageInput.toIntOrNull() ?: run {
            Toast.makeText(this, getString(R.string.invalid_age), Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar o gênero selecionado
        val genderMultiplier = when (spinnerGender.selectedItemId) {
            0L -> "M"
            1L -> "F"
            else -> {
                // Gênero não selecionado
                return
            }
        }

        // Verificar o nível de atividade selecionado
        val activityLevel = when (spinnerActivityLevel.selectedItemId) {
            0L -> 1.2
            1L -> 1.375
            2L -> 1.55
            3L -> 1.725
            4L -> 1.9
            else -> {
                // Nível de atividade não selecionado
                return
            }
        }

        // Construir e salvar o perfil BMR
        val bmr = BasalMetabolicRate.build(
            weight = weight,
            height = height,
            age = age,
            gender = genderMultiplier,
            activityLevel = activityLevel
        )

        // Salvar o perfil BMR e fechar a atividade se for bem-sucedido
        if (bmr.save(this)) {
            finish()
        }
    }

}
