package com.example.anarcomarombismo

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import com.example.anarcomarombismo.Adapters.FoodAdapter
import com.example.anarcomarombismo.Controller.JSON
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class formDailyCalories : AppCompatActivity() {
    private var lastClickTime: Long = 0
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
    private lateinit var saveCaloriesButton: Button
    private var currentFood: Food? = null
    private var temporaryCalcule: Double = 0.0
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
        saveCaloriesButton = findViewById(R.id.saveCaloriesButton)

        //getInputExtra
        getDailyCalories()

        searchButton.setOnClickListener {
            searchFood(searchEditText.text.toString())
        }

        addFoodButton.setOnClickListener {
            try {
                saveCaloriesButton.isEnabled = true
                addFoodToDailyList()
            } catch (e: Exception) {
                Toast.makeText(this, "Error adding food to daily list", Toast.LENGTH_SHORT).show()
            }
        }

        saveCaloriesButton.setOnClickListener {
            saveDailyCalories()
        }

        gramsEditText.addTextChangedListener {
            // get gramsEditText value and calculate the total calories
            calculeTotalCalories(it)
        }

        listFoodsView.setOnItemClickListener { parent, view, position, id ->
            selectedFood(parent.getItemAtPosition(position) as Food)
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastClickTime < 300) {  // Duplo clique detectado
                onDoubleClick(parent.getItemAtPosition(position) as Food)
            }
            lastClickTime = currentTime
        }

        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchFood(searchEditText.text.toString())
            true
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
            // will excluse the current dailyCalories from the list and return to the previous activity
            val currentDate = dailyCalories.date
            var cache = Cache()
            if (cache.hasCache(this, "dailyCalories")) {
                val dailyCaloriesListJson = cache.getCache(this, "dailyCalories")
                val jsonUtil = JSON()
                var dailyCaloriesList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
                val dailyCaloriesListFiltered = dailyCaloriesList.filter { it.date == currentDate }
                if (dailyCaloriesListFiltered.isNotEmpty()) {
                    dailyCaloriesList = dailyCaloriesList.minus(dailyCaloriesListFiltered)
                    cache.setCache(this, "dailyCalories", jsonUtil.toJson(dailyCaloriesList))
                    Toast.makeText(this, "Daily calories removed successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        setFoodToFoodList()
    }

    override fun onResume() {
        super.onResume()
        if (dailyCaloriesFoods.getFoodList().size !== dailyCalories.foodsList.size) {
            dailyCalories.foodsList = dailyCaloriesFoods.getFoodList()
            dailyCalories.recalculateCalories()
            totalCaloriesLabel.text = "Total: ${String.format("%.1f", dailyCalories.calorieskcal)} kcal"
        }
        setFoodToFoodList()
    }

    private fun onDoubleClick(food: Food?) {
        // Ação a ser executada no duplo clique
        // Exemplo: abrir uma nova activity
        val intent = Intent(this, formFoods::class.java)
        intent.putExtra("foodID", food?.foodNumber)
        this.startActivity(intent)
        Toast.makeText(this, "Opening food details", Toast.LENGTH_SHORT).show()
    }
    fun getDailyCaloriesByDate(selectedDate: String) {
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
                }
            }
        }
    }

    fun getDailyCalories() {
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

    fun callDailyCaloriesFoods() {
        try {
            var dailyCaloriesFoods = Intent(this, dailyCaloriesFoods::class.java)
            var jsonUtil = JSON()
            dailyCaloriesFoods.putExtra("foodsList", dailyCalories.foodsList.let { jsonUtil.toJson(it) })
            startActivity(dailyCaloriesFoods)
        } catch (e: Exception) {
            println(RuntimeException("Error calling daily calories foods: $e"))
        }
    }
    fun setFoodToFoodList() {
        GlobalScope.launch(Dispatchers.IO) {
            val jsonUtil = JSON()
            val cache = Cache()
            try {
                val foodNutritionList: List<Food>
                if (cache.hasCache(this@formDailyCalories,"Alimentos")) {
                    foodNutritionList = jsonUtil.fromJson(cache.getCache(this@formDailyCalories,"Alimentos"), Array<Food>::class.java).toList()
                    println("Lido de cache")
                } else {
                    val jsonContent = resources.openRawResource(R.raw.nutritional_table).bufferedReader()
                            .use { it.readText() }
                    foodNutritionList = jsonUtil.fromJson(jsonContent, Array<Food>::class.java).toList()
                    cache.setCache(this@formDailyCalories,"Alimentos",jsonContent)
                }
                withContext(Dispatchers.Main) {
                    val adapter = FoodAdapter(this@formDailyCalories, foodNutritionList)
                    listFoodsView.adapter = adapter
                }
            } catch (e: Exception) {
                println(RuntimeException("Erro ao ler o arquivo JSON: $e"))
            }
        }
    }
    fun calculeTotalCalories(it: CharSequence?) {
        try {
            val grams = it.toString().toDoubleOrNull()
            val temporaryCalcule = currentFood!!.energyKcal.toDouble() * (grams!! / 100)
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
        } catch (e: Exception) {
            println(RuntimeException("Error handling food click: $e"))
        }
    }
    fun searchFood(value: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val jsonUtil = JSON()
            val cache = Cache()
            try {
                val foodNutritionList: List<Food>
                if (cache.hasCache(this@formDailyCalories, "Alimentos")) {
                    foodNutritionList = jsonUtil.fromJson(cache.getCache(this@formDailyCalories, "Alimentos"), Array<Food>::class.java).toList()
                } else {
                    val jsonContent = withContext(Dispatchers.IO) {
                        resources.openRawResource(R.raw.nutritional_table).bufferedReader()
                            .use { it.readText() }
                    }
                    foodNutritionList = jsonUtil.fromJson(jsonContent, Array<Food>::class.java).toList()
                    cache.setCache(this@formDailyCalories, "Alimentos", jsonContent)
                }
                val foodNutritionListFiltered = foodNutritionList.filter { it.foodDescription.contains(value, ignoreCase = true) }
                val adapter = FoodAdapter(this@formDailyCalories, foodNutritionListFiltered)
                listFoodsView.adapter = adapter
            } catch (e: Exception) {
                println(RuntimeException("Erro ao ler o arquivo JSON: $e"))
            }
        }
    }
    fun addFoodToDailyList() {
        try {
            currentFood?.let { food ->
                gramsEditText.text.toString().toDoubleOrNull()?.let { grams ->
                    val grams = gramsEditText.text.toString().toDouble()
                    val temporaryCalcule = this.currentFood!!.energyKcal.toDouble() * (grams/100)
                    val totalCalories = String.format("%.1f", temporaryCalcule + dailyCalories.calorieskcal)
                    Toast.makeText(this, "$temporaryCalcule kcal + ${dailyCalories.calorieskcal} kcal = $totalCalories kcal", Toast.LENGTH_SHORT).show()
                    food.grams = grams
                    dailyCalories.date = editTextDate.text.toString()
                    dailyCalories.addFood(food)
                    totalCaloriesLabel.text = "Total: ${String.format("%.1f", dailyCalories.calorieskcal)} kcal"
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

    fun saveDailyCalories() {
        addFoodToDailyList()
        val cache = Cache()
        val jsonUtil = JSON()
        try {
            var dailyCaloriesList: List<DailyCalories> = if (cache.hasCache(this, "dailyCalories")) {
                val dailyCaloriesListJson = cache.getCache(this, "dailyCalories")
                jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
            } else {
                emptyList()
            }

            val formattedDate = editTextDate.text.toString()
            val dailyCaloriesListFiltered = dailyCaloriesList.filter { it.date == formattedDate }
            dailyCaloriesList = dailyCaloriesList.minus(dailyCaloriesListFiltered)
            dailyCaloriesList = dailyCaloriesList.plus(dailyCalories)
            cache.setCache(this, "dailyCalories", jsonUtil.toJson(dailyCaloriesList))
            Toast.makeText(this, "Daily calories saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            println(RuntimeException("Error saving daily calories: $e"))
        }
    }

}
