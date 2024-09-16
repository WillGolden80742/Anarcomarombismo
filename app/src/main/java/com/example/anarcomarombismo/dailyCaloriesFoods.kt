package com.example.anarcomarombismo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories_foods)
        searchFoodListEditText = findViewById(R.id.searchFoodListEditText)
        searchFoodListButton = findViewById(R.id.searchFoodListButton)

        // Initialize views from layout
        listView = findViewById(R.id.listFoodsView)

        try {
            // Get the food list from the intent
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
        // Get the food list from the JSON
        val jsonUtil = JSON()
        setFoodList(intent.getStringExtra("foodsList")?.let { jsonUtil.fromJson(it, Array<Food>::class.java) }?.toList()!!)
        dailyCaloriesDate = intent.getStringExtra("dailyCaloriesDate")!!
        searchFoodList("")
    }

    private fun searchFoodList(value: String) {
        // Search for food in the list
        val filteredList = foodList.filter { it.foodDescription.contains(value, ignoreCase = true) }
        val adapter = FoodAdapter(this, filteredList,"dailyCaloriesFoods")
        listView.adapter = adapter
    }

    fun removeFood(food: Food) {
        // get the selected food and remove it from the list
        foodList = foodList.minus(food)
        // update the list view
        val adapter = FoodAdapter(this, foodList,"dailyCaloriesFoods")
        listView.adapter = adapter
        if (foodList.isEmpty()) {
            val dailyCalories = createDailyCalories()
            dailyCalories.remove(this)
            finish()
        } else {
            val dailyCalories = createDailyCalories()
            dailyCalories.save(this)
        }
    }

    private fun createDailyCalories(): DailyCalories {
        return DailyCalories().apply {
            date = dailyCaloriesDate
            foodsList = foodList
            recalculateCalories()
        }
    }

}
