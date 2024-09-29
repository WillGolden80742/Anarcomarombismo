package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Adapter.DailyCaloriesAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Forms.formDailyCalories
import com.example.anarcomarombismo.Forms.formFood
import com.example.anarcomarombismo.Forms.formMacros
import com.google.android.material.floatingactionbutton.FloatingActionButton

class dailyCalories : AppCompatActivity() {
    private lateinit var caloriesFoodList: ListView
    private lateinit var addCaloriesButton: Button
    private lateinit var addNewFoodButton: Button
    private lateinit var progressContainer: LinearLayout
    private lateinit var showStatisticsButton: FloatingActionButton
    private lateinit var editStatisticsButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories)
        caloriesFoodList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addFoodFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        progressContainer = findViewById(R.id.progressContainer)
        showStatisticsButton = findViewById(R.id.showStatisticsButton)
        editStatisticsButton = findViewById(R.id.editStatisticsButton)
        addCaloriesButton.setOnClickListener {
            callFormDailyCalories()
        }
        showStatisticsButton.setOnClickListener {
            if (!progressContainer.isVisible) {
                progressContainer.isVisible = true
                editStatisticsButton.isVisible = true
                showStatisticsButton.setImageResource(R.drawable.ic_fluent_dismiss_24_filled)
            } else {
                progressContainer.isVisible = false
                editStatisticsButton.isVisible = false
                showStatisticsButton.setImageResource(R.drawable.ic_fluent_arrow_growth_24_filled)
            }
        }

        progressContainer.setOnClickListener {
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
        progressContainer.isVisible = false
        editStatisticsButton.isVisible = false
        showStatisticsButton.setImageResource(R.drawable.ic_fluent_arrow_growth_24_filled)
        super.onResume()
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



}