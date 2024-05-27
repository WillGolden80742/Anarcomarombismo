package com.example.anarcomarombismo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON

class dailyCaloriesFoods : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
    private lateinit var removeFoodButton: Button
    private lateinit var saveFoodButton: Button
    private lateinit var foodSelectedTextView: TextView
    private lateinit var selectedFood : Food
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
        removeFoodButton = findViewById(R.id.removeFoodButton)
        saveFoodButton = findViewById(R.id.saveFoodButton)
        foodSelectedTextView = findViewById(R.id.foodSelectedTextView)

        try {
            // Get the food list from the intent
            if (intent.hasExtra("foodsList")) {
                val jsonUtil = JSON()
                setFoodList(intent.getStringExtra("foodsList")?.let { jsonUtil.fromJson(it, Array<Food>::class.java) }?.toList()!!)
                var foodList = getFoodList()
                val adapter = FoodAdapter(this, foodList)
                listView.adapter = adapter
            }
        } catch (e: Exception) {
            println("Error loading food list: $e")
        }

        // Set onClickListener for removeFoodButton (if needed)
        removeFoodButton.setOnClickListener {
            removeFoodButton.isEnabled = false
            // get the selected food and remove it from the list
            foodList = foodList.minus(selectedFood)
            // update the list view
            val adapter = FoodAdapter(this, foodList)
            listView.adapter = adapter
            foodSelectedTextView.text = getString(R.string.selecione_alimento)
        }

        // Set onClickListener for saveFoodButton (if needed)
        saveFoodButton.setOnClickListener {
           // Save the food list in the intent and finish the activity
            finish()
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val food = parent.getItemAtPosition(position) as Food
            foodSelectedTextView.text = food.foodDescription
            selectedFood = food
            removeFoodButton.isEnabled = true
        }
    }
}
