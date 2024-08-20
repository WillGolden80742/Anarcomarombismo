package com.example.anarcomarombismo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.JSON
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
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.FoodSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var temporaryCalcule: Double = 0.0
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
                saveDailyCalories()
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

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        editTextDate.setOnClickListener {
            // Cria um DatePickerDialog para selecionar a data
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Define a data selecionada no EditText
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear" // Mês é base 0, por isso adicionamos 1
                    if (selectedDate != editTextDate.text) {
                        // do format 00/00/0000
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
                        editTextDate.text = formattedDate
                        // get by cache if selectedDate exists
                        getDailyCaloriesByDate(formattedDate.toString())
                    }
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        removeDailyCaloriesButton.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                removeDailyCalories()
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
            // saveDailyCalories()
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
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            if (cache.hasCache(this@formDailyCalories, "dailyCalories")) {
                val dailyCaloriesListJson = cache.getCache(this@formDailyCalories, "dailyCalories")
                val jsonUtil = JSON()
                val dailyCaloriesList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
                val dailyCaloriesListFiltered = dailyCaloriesList.filter { it.date == selectedDate }
                withContext(Dispatchers.Main) {
                    if (dailyCaloriesListFiltered.isNotEmpty()) {
                        dailyCalories = dailyCaloriesListFiltered[0]
                        dailyCaloriesFoods.setFoodList(dailyCalories.foodsList)
                        totalCaloriesLabel.text = "Total: ${dailyCalories.calorieskcal} kcal"
                    } else {
                        dailyCalories = DailyCalories()
                        dailyCalories.date = selectedDate
                        dailyCaloriesFoods.setFoodList(dailyCalories.foodsList)
                        totalCaloriesLabel.text = "Total: ${dailyCalories.calorieskcal} kcal"
                    }
                    // se a lista de alimentos estiver vazia, desabilita o botão
                    seeFoodsButton.isEnabled = dailyCalories.foodsList.isNotEmpty()
                }
            }
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
            var jsonUtil = JSON()
            dailyCaloriesFoods.putExtra("foodsList", dailyCalories.foodsList.let { jsonUtil.toJson(it) })
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
            temporaryCalcule = currentFood!!.energyKcal.toDouble() * (gramsEditText.text.toString().toDouble() / 100)
            val temporaryTotal = temporaryCalcule + dailyCalories.calorieskcal
            totalCaloriesLabel.text = "Total: ${String.format("%.1f", temporaryTotal)} kcal"
            nameFoodLabel.text = currentFood!!.foodDescription
            gramsEditText.isEnabled = true
        } catch (e: Exception) {
            println(RuntimeException("Error handling food click: $e"))
        }
    }

    private fun searchFood(query: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val foodList = loadFoodList()
                val filteredList = filterFoodList(foodList, query)
                updateListView(filteredList)
                searchFoodAsync(query)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun loadFoodList(): List<Food> {
        val cacheKey = "Alimentos"
        val cache = Cache()
        val jsonUtil = JSON()

        return if (cache.hasCache(this@formDailyCalories, cacheKey)) {
            jsonUtil.fromJson(cache.getCache(this@formDailyCalories, cacheKey), Array<Food>::class.java).toList()
        } else {
            val jsonContent = loadJsonFromResource(R.raw.nutritional_table)
            cache.setCache(this@formDailyCalories, cacheKey, jsonContent)
            jsonUtil.fromJson(jsonContent, Array<Food>::class.java).toList()
        }
    }

    private suspend fun loadJsonFromResource(resourceId: Int): String {
        return withContext(Dispatchers.IO) {
            resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        }
    }

    private fun filterFoodList(foodList: List<Food>, query: String): List<Food> {
        return if (query.isEmpty()) {
            foodList
        } else {
            foodList.filter { it.foodDescription.contains(query, ignoreCase = true) }
        }
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
        if (currentFood !== null) {
            try {
                currentFood?.let { food ->
                    gramsEditText.text.toString().toDoubleOrNull()?.let { grams ->
                        val grams = gramsEditText.text.toString().toDouble()
                        val temporaryCalcule =
                            this.currentFood!!.energyKcal.toDouble() * (grams / 100)
                        val totalCalories =
                            String.format("%.1f", temporaryCalcule + dailyCalories.calorieskcal)
                        Toast.makeText(
                            this,
                            "$temporaryCalcule kcal + ${String.format("%.1f", dailyCalories.calorieskcal)} kcal = $totalCalories kcal",
                            Toast.LENGTH_SHORT
                        ).show()
                        food.grams = grams
                        dailyCalories.date = editTextDate.text.toString()
                        dailyCalories.addFood(food)
                        totalCaloriesLabel.text =
                            "Total: ${String.format("%.1f", dailyCalories.calorieskcal)} kcal"
                        currentFood = null
                    } ?: run {
                        gramsEditText.error = "Invalid input"
                    }
                } ?: run {
                    throw RuntimeException("Current food is null")
                }
            } catch (e: Exception) {
                println(RuntimeException("Error adding food to daily list: $e"))
            }
        }
    }

    private fun saveDailyCalories() {
        val validFoodsList = dailyCalories.foodsList.filter { it.foodDescription != "NO_DESCRIPTION" }
        if (validFoodsList.isEmpty() && currentFood == null) {
            removeDailyCalories()
            return
        }

        dailyCalories.foodsList = validFoodsList
        updateDailyCaloriesCache()
        resetUI()
    }

    private fun updateDailyCaloriesCache() {
        val cache = Cache()
        val jsonUtil = JSON()
        try {
            val dailyCaloriesList = getExistingDailyCaloriesList(cache, jsonUtil)
            val updatedCaloriesList = dailyCaloriesList.filterNot { it.date == getCurrentFormattedDate() } + dailyCalories
            cache.setCache(this, "dailyCalories", jsonUtil.toJson(updatedCaloriesList))
        } catch (e: Exception) {
            handleError(e, "Error saving daily calories")
        }
    }

    private fun getExistingDailyCaloriesList(cache: Cache, jsonUtil: JSON): List<DailyCalories> {
        return if (cache.hasCache(this, "dailyCalories")) {
            val dailyCaloriesListJson = cache.getCache(this, "dailyCalories")
            jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
        } else {
            emptyList()
        }
    }

    private fun getCurrentFormattedDate(): String {
        return editTextDate.text.toString()
    }

    private fun resetUI() {
        gramsEditText.isEnabled = false
        gramsEditText.setText("100")
        nameFoodLabel.text = this.getString(R.string.select_food)
        dailyCaloriesFoods.setFoodList(dailyCalories.foodsList)
    }

    private fun handleError(e: Exception, message: String) {
        println(RuntimeException("$message: $e"))
    }

    private fun removeDailyCalories() {
        val currentDate = dailyCalories.date
        val cacheKey = "dailyCalories"
        val cache = Cache()

        cache.getCache(this, cacheKey)?.let { dailyCaloriesListJson ->
            val dailyCaloriesList = parseDailyCaloriesList(dailyCaloriesListJson)
            val updatedCaloriesList = removeCaloriesForDate(dailyCaloriesList, currentDate)

            if (updatedCaloriesList.size != dailyCaloriesList.size) {
                cache.setCache(this, cacheKey, toJson(updatedCaloriesList))
                showToast(R.string.daily_calories_removed_successfully)
                resetDailyCalories(currentDate)
            }
        }

        finish()
    }

    private fun parseDailyCaloriesList(json: String): List<DailyCalories> {
        return JSON().fromJson(json, Array<DailyCalories>::class.java).toList()
    }

    private fun removeCaloriesForDate(
        dailyCaloriesList: List<DailyCalories>,
        date: String
    ): List<DailyCalories> {
        return dailyCaloriesList.filterNot { it.date == date }
    }

    private fun toJson(dailyCaloriesList: List<DailyCalories>): String {
        return JSON().toJson(dailyCaloriesList)
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun resetDailyCalories(date: String) {
        dailyCalories = DailyCalories().apply { this.date = date }
        dailyCaloriesFoods.run { setFoodList(dailyCalories.foodsList) }
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
