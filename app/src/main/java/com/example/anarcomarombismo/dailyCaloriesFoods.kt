package com.example.anarcomarombismo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON

class dailyCaloriesFoods : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
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

        // Initialize views from layout
        listView = findViewById(R.id.listFoodsView)

        try {
            // Get the food list from the intent
            if (intent.hasExtra("foodsList")) {
                val jsonUtil = JSON()
                setFoodList(intent.getStringExtra("foodsList")?.let { jsonUtil.fromJson(it, Array<Food>::class.java) }?.toList()!!)
                var foodList = getFoodList()
                val adapter = FoodAdapter(this, foodList,"dailyCaloriesFoods")
                listView.adapter = adapter
            }
        } catch (e: Exception) {
            println("Error loading food list: $e")
        }

    }

    fun removeFood(food: Food) {
        // get the selected food and remove it from the list
        foodList = foodList.minus(food)
        // update the list view
        val adapter = FoodAdapter(this, foodList,"dailyCaloriesFoods")
        listView.adapter = adapter
        if (foodList.isEmpty()) {
            finish()
        }
    }
}
