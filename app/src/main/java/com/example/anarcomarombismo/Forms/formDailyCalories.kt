package com.example.anarcomarombismo.Forms

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.Util.JSON
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.FoodSearch
import com.example.anarcomarombismo.Controller.Util.Calendaries
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCaloriesFoods
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class formDailyCalories : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var listFoodsView: ListView
    private lateinit var totalCaloriesLabel: TextView
    private lateinit var nameFoodLabel: TextView
    private var dailyCalories: DailyCalories = DailyCalories()
    private lateinit var editTextDate: TextView
    private lateinit var seeFoodsButton: Button
    private lateinit var removeDailyCaloriesButton: Button
    private lateinit var gramsEditText: EditText
    private lateinit var addFoodButton: Button
    private var currentFood: Food? = null
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300
    private var lastClickTime: Long = 0
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_daily_calories)

        // Inicializar os elementos do layout
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        listFoodsView = findViewById(R.id.listFoodsView)
        totalCaloriesLabel = findViewById(R.id.totalCaloriesLabel)
        nameFoodLabel = findViewById(R.id.nameFoodLabel)
        editTextDate = findViewById(R.id.editTextDate)
        gramsEditText = findViewById(R.id.gramsEditText)
        addFoodButton = findViewById(R.id.addFoodButton)
        seeFoodsButton = findViewById(R.id.seeFoodsButton)
        removeDailyCaloriesButton = findViewById(R.id.removeDailyCaloriesButton)

        loading()
        //getInputExtra
        getDailyCalories()

        searchButton.setOnClickListener {
            searchFood(searchEditText.text.toString())
            hideKeyboard(this.currentFocus ?: View(this))
        }

        addFoodButton.setOnClickListener {
            try {
                addFoodToDailyList()
                if(dailyCalories.save(this)) {
                    addFoodButton.isEnabled = false
                    nameFoodLabel.text = getString(R.string.select_food)
                    gramsEditText.isEnabled = false
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error adding food to daily list", Toast.LENGTH_SHORT).show()
            }
        }


        gramsEditText.addTextChangedListener {
            // get gramsEditText value and calculate the total calories
            calculeTotalCalories(it)
        }


        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchFood(searchEditText.text.toString())
            hideKeyboard(this.currentFocus ?: View(this))
        }

        searchEditText.setOnClickListener {
            searchEditText.setText("")
        }

        seeFoodsButton.setOnClickListener {
            callDailyCaloriesFoods()
        }

        editTextDate.setOnClickListener {
            selectDate()
        }

        removeDailyCaloriesButton.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                if(dailyCalories.remove(this)) {
                    finish()
                }
            } else {
                Toast.makeText(this,
                    getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
            }
            lastClickTime = clickTime
        }

    }
    override fun onResume() {
        super.onResume()
        setFoodToFoodList()
        if (dailyCaloriesFoods.getFoodList().size !== dailyCalories.foodsList.size) {
            dailyCalories.foodsList = dailyCaloriesFoods.getFoodList()
            dailyCalories.recalculateCalories()
            totalCaloriesLabel.text = "Total: ${String.format("%.1f", dailyCalories.calorieskcal)} kcal"
            if (dailyCalories.foodsList.isEmpty()) {
                seeFoodsButton.isEnabled = false
            }
            currentFood = null
        }
    }

    private fun selectDate () {
        val calendar = Calendar.getInstance()
        val maxDate = calendar.timeInMillis
        Calendaries().selectDate(this, editTextDate, maxDate) {
            getDailyCaloriesByDate(maxDate.toString())
        }
    }

    private fun loading() {
        var foodList = emptyList<Food>()
        val food = Food()
        foodList = foodList.plus(food)
        listFoodsView.adapter = FoodAdapter(this,foodList,"loading")
    }
    private fun hideKeyboard (view: View): Boolean {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        return true
    }

    private fun getDailyCaloriesByDate(selectedDate: String) {
        DailyCalories().load(this, selectedDate) { dailyCaloriesLoaded ->
            dailyCalories = dailyCaloriesLoaded
            dailyCaloriesFoods.setFoodList(dailyCalories.foodsList)
            totalCaloriesLabel.text = "Total: ${dailyCalories.calorieskcal} kcal"
            seeFoodsButton.isEnabled = dailyCalories.foodsList.isNotEmpty()
        }
    }

    private fun getDailyCalories() {
        if (intent.hasExtra("dailyCaloriesDate")) {
            val selectedDate = intent.getStringExtra("dailyCaloriesDate")
            editTextDate.text = selectedDate
            getDailyCaloriesByDate(selectedDate.toString())
        } else {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            editTextDate.text = formattedDate
            getDailyCaloriesByDate(formattedDate)
        }
    }

    private fun callDailyCaloriesFoods() {
        try {
            var dailyCaloriesFoods = Intent(this, dailyCaloriesFoods::class.java)
            var json = JSON()
            dailyCaloriesFoods.putExtra("foodsList", dailyCalories.foodsList.let { json.toJson(it) })
            dailyCaloriesFoods.putExtra("dailyCaloriesDate", dailyCalories.date)
            startActivity(dailyCaloriesFoods)
        } catch (e: Exception) {
            println(RuntimeException("Error calling daily calories foods: $e"))
        }
    }
    private fun setFoodToFoodList() {
        searchFood(searchEditText.text.toString())
    }

    private fun calculeTotalCalories(it: CharSequence?) {
        try {
            val grams = it.toString().toDoubleOrNull().let { it ?: 0.0 }
            val currentCalorie = currentFood?.energyKcal?.replace(",", ".")?.toDouble().let { it?: 0.0 }
            val temporaryCalcule = currentCalorie * (grams!! / 100.0)
            val temporaryTotal = temporaryCalcule + dailyCalories.calorieskcal
            totalCaloriesLabel.text = "Total: ${String.format("%.1f", temporaryTotal)} kcal"
        } catch (e: Exception) {
            println(RuntimeException("Error calculating total calories: $e"))
            totalCaloriesLabel.text = "Total: Erro kcal"
        }
    }
    fun selectedFood(food: Food) {
        try {
            addFoodButton.isEnabled = true
            currentFood = food

            val grams = gramsEditText.text.toString().toDoubleOrNull() ?: 0.0
            val energy = currentFood?.energyKcal?.toDouble() ?: 0.0
            val temporaryCalcule = energy * (grams / 100)
            val temporaryTotal = temporaryCalcule + dailyCalories.calorieskcal

            totalCaloriesLabel.text = "Total: ${String.format("%.1f", temporaryTotal)} kcal"
            nameFoodLabel.text = currentFood?.foodDescription ?: "Unknown food"
            gramsEditText.isEnabled = true
        } catch (e: Exception) {
            println("Error handling food click: ${e.message}")
        }
    }

    private fun searchFood(query: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val foodList = Food().loadList(this@formDailyCalories)
                val filteredList = filterFoodList(foodList, query)
                updateListView(filteredList)
                searchFoodAsync(query)
            } catch (e: Exception) {
                handleException(e)
                println("erro ao carregar : $e")
            }
        }
    }
    private fun filterFoodList(foodList: List<Food>, query: String): List<Food> {
        return if (query.isEmpty()) {
            foodList
        } else {
            foodList.filter { normalizeString(it.foodDescription).contains(normalizeString(query), ignoreCase = true) }
        }
    }

    private fun normalizeString(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }

    private fun updateListView(filteredList: List<Food>) {
        val adapter = FoodAdapter(this@formDailyCalories, filteredList)
        listFoodsView.adapter = adapter
    }

    private fun appendListView(foodList: Food) {
        val adapter = listFoodsView.adapter as FoodAdapter
        adapter.add(foodList)
    }

    private fun handleException(e: Exception) {
        println(RuntimeException("Error reading the JSON file: $e"))
    }

    private fun addFoodToDailyList() {
        seeFoodsButton.isEnabled = true
        currentFood?.let { food ->
            val grams = getValidatedGrams() ?: return
            updateCaloriesAndUI(food, grams)
        } ?: showFoodError()
    }

    private fun getValidatedGrams(): Double? {
        return gramsEditText.text.toString().toDoubleOrNull()?.also {
            if (it <= 0) {
                gramsEditText.error = "Invalid input"
            }
        }
    }

    private fun updateCaloriesAndUI(food: Food, grams: Double) {
        val temporaryCalories = calculateCalories(food, grams)
        showToastWithCalories(temporaryCalories)
        food.grams = grams
        dailyCalories.date = editTextDate.text.toString()
        dailyCalories.addFood(food)
        updateTotalCaloriesUI()
        currentFood = null
    }

    private fun calculateCalories(food: Food, grams: Double): Double {
        return food.energyKcal.toDouble() * (grams / 100)
    }

    private fun showToastWithCalories(temporaryCalories: Double) {
        val totalCalories = temporaryCalories + dailyCalories.calorieskcal
        val message = "${String.format("%.1f", temporaryCalories)} kcal + ${String.format("%.1f", dailyCalories.calorieskcal)} kcal = ${String.format("%.1f", totalCalories)} kcal"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateTotalCaloriesUI() {
        totalCaloriesLabel.text = "Total: ${String.format("%.1f", dailyCalories.calorieskcal)} kcal"
    }

    private fun showFoodError() {
        throw RuntimeException("Current food is null")
    }

    // Usar Coroutine para chamada assíncrona
    private fun searchFoodAsync(query: String) {
        if (query.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = FoodSearch().searchFood(this@formDailyCalories, query)
                withContext(Dispatchers.Main) {

                    for (foodSearch in result) {
                        getFoodByURLAsync(
                            foodSearch.href,
                            foodSearch.grams.toDoubleOrNull() ?: 100.0
                        )
                    }
                }
            }
        }
    }

    private fun getFoodByURLAsync (url: String, grams:Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = FoodSearch().getFoodByURL(this@formDailyCalories,url,grams)
            withContext(Dispatchers.Main) {
                if (result.foodDescription != "NO_DESCRIPTION") {
                    appendListView(result)
                }
            }
        }
    }


}