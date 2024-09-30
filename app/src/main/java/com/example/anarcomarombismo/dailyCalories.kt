package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Adapter.DailyCaloriesAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Forms.formDailyCalories
import com.example.anarcomarombismo.Forms.formFood
import com.example.anarcomarombismo.Forms.formMacros

class dailyCalories : AppCompatActivity() {
    private lateinit var caloriesFoodList: ListView
    private lateinit var addCaloriesButton: Button
    private lateinit var addNewFoodButton: Button
    private lateinit var progressBarContainer: LinearLayout
    private lateinit var editStatisticsButton: Button
    private lateinit var caloriesProgressBar: ProgressBar
    private lateinit var carbsProgressBar: ProgressBar
    private lateinit var fatsProgressBar: ProgressBar
    private lateinit var proteinsProgressBar: ProgressBar
    private lateinit var dietaryFiberProgressBar: ProgressBar
    private lateinit var caloriesLabel: TextView
    private lateinit var carbsLabel: TextView
    private lateinit var lipidsLabel: TextView
    private lateinit var proteinsLabel: TextView
    private lateinit var dietaryFiberLabel: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories)
        caloriesFoodList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addFoodFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        progressBarContainer = findViewById(R.id.progressBarContainer)
        editStatisticsButton = findViewById(R.id.editStatisticsButton)
        caloriesProgressBar = findViewById(R.id.caloriesProgressBar)
        carbsProgressBar = findViewById(R.id.carbsProgressBar)
        fatsProgressBar = findViewById(R.id.fatsProgressBar)
        proteinsProgressBar = findViewById(R.id.proteinsProgressBar)
        dietaryFiberProgressBar = findViewById(R.id.dietaryFiberProgressBar)
        caloriesLabel = findViewById(R.id.caloriesLabel)
        carbsLabel = findViewById(R.id.carbsLabel)
        lipidsLabel = findViewById(R.id.lipidsLabel)
        proteinsLabel = findViewById(R.id.proteinsLabel)
        dietaryFiberLabel = findViewById(R.id.dietaryFiberLabel)

        addCaloriesButton.setOnClickListener {
            callFormDailyCalories()
        }

        progressBarContainer.setOnClickListener {
            val intent = Intent(this, formMacros::class.java)
            startActivity(intent)
        }

        editStatisticsButton.setOnClickListener {
            val intent = Intent(this, formMacros::class.java)
            startActivity(intent)
        }

        caloriesFoodList.setOnItemClickListener { parent, _, position, _ ->
            val dailyCalories = parent.getItemAtPosition(position) as DailyCalories
            val intent = Intent(this, formDailyCalories::class.java)
            try {
                intent.putExtra("dailyCaloriesDate", dailyCalories.date)
                startActivity(intent)
            } catch (e: Exception) {
                println("Erro ao chamar a tela de calorias diárias: $e")
            }
        }

        addNewFoodButton.setOnClickListener {
            callFoodForm()
        }
    }
    override fun onResume() {
        super.onResume()
        loadMacroTarget()
        caloriesFoodList.adapter = DailyCaloriesAdapter(this,
            DailyCalories().fetchAll(this).ifEmpty {
                listOf(DailyCalories())
            }
        )
    }
    private fun callFormDailyCalories() {
        try {
            startActivity(Intent(this, formDailyCalories::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    private fun callFoodForm() {
        try {
            startActivity(Intent(this, formFood::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de alimentos: $e")
        }
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
            dietaryFiberLabel,
            true
        )
    }

}