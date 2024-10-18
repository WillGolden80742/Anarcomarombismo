package com.example.anarcomarombismo

import android.os.Bundle
import android.widget.ListView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.Adapter.FoodAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.Controller.Util.StringHandler

class dailyCaloriesFoods : AppCompatActivity() {
    // Declare elements from layout
    private lateinit var listView: ListView
    private lateinit var searchView: SearchView
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
        searchView = findViewById(R.id.searchView)
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
        // Set listener for search queries in the SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchFoodList(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optionally filter the list as the user types
                newText?.let {
                    searchFoodList(it)
                }
                return true
            }
        })

        // Set the searchView to clear the query on clicking the search bar
        searchView.setOnClickListener {
            searchView.isIconified = false
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
