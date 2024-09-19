package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.DailyCaloriesAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Forms.formDailyCalories
import com.example.anarcomarombismo.Forms.formFood

class dailyCalories : AppCompatActivity() {
    private lateinit var caloriesFoodList: ListView
    private lateinit var addCaloriesButton: Button
    private lateinit var addNewFoodButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories)
        caloriesFoodList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addFoodFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        addCaloriesButton.setOnClickListener {
            callFormDailyCalories()
        }

        caloriesFoodList.setOnItemClickListener { parent, view, position, id ->
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
    // onResume
    override fun onResume() {
        super.onResume()
        DailyCalories().loadList(this) { dailyCaloriesList ->
            val adapter = DailyCaloriesAdapter(this, dailyCaloriesList)
            caloriesFoodList.adapter = adapter
        }
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