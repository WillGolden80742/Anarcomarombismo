package com.example.anarcomarombismo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.anarcomarombismo.Controller.Adapter.FoodAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.Controller.Util.StringHandler

class dailyCaloriesFoods : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
    private lateinit var searchFoodListEditText: EditText
    private lateinit var searchFoodListButton: Button
    private lateinit var dailyCaloriesDate: String
    companion object {
        private var foodList: List<Food> = emptyList()

        fun getFoodList(): List<Food> {
            return foodList
        }

        fun setFoodList(list: List<Food>) {
            foodList = list
        }
    }
    private fun initializeUIComponents() {
        searchFoodListEditText = findViewById(R.id.searchFoodListEditText)
        searchFoodListButton = findViewById(R.id.searchFoodListButton)
        listView = findViewById(R.id.listFoodsView)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories_foods)
        initializeUIComponents()
        try {
            if (intent.hasExtra("foodsList")) {
                loadFoodList()
            }
        } catch (e: Exception) {
            println("Error loading food list: $e")
        }
        searchFoodListEditText.setOnEditorActionListener { _, _, _ ->
            val value = searchFoodListEditText.text.toString()
            searchFoodList(value)
            true
        }
        searchFoodListEditText.setOnClickListener {
            searchFoodListEditText.setText("")
        }
        searchFoodListButton.setOnClickListener {
            val value = searchFoodListEditText.text.toString()
            searchFoodList(value)
        }
    }

    private fun loadFoodList() {
        setFoodList(intent.getStringExtra("foodsList")?.let { JSON.fromJson(it, Array<Food>::class.java) }?.toList()!!)
        dailyCaloriesDate = intent.getStringExtra("dailyCaloriesDate")!!
        searchFoodList("")
    }

    private fun searchFoodList(value: String) {
        val filteredList = foodList.filter { StringHandler.containsQuery(it.foodDescription,value) }
        val adapter = FoodAdapter(this, filteredList,"dailyCaloriesFoods")
        listView.adapter = adapter
    }

    fun removeFood(food: Food) {
        foodList = foodList.minus(food)
        listView.adapter = FoodAdapter(this, foodList,"dailyCaloriesFoods")
        if (foodList.isEmpty()) {
            buildDailyCalories().remove(this)
            finish()
        } else {
            buildDailyCalories().save(this)
        }
    }

    private fun buildDailyCalories(): DailyCalories {
        return DailyCalories.build(
            dailyCaloriesDate,
            foodList
        )
    }

}
