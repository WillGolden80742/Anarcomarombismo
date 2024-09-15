package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON
import java.util.UUID

class formFoods : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextGrams: EditText
    private lateinit var editTextProtein: EditText
    private lateinit var editTextCarbohydrate: EditText
    private lateinit var editTextLipids: EditText
    private lateinit var editTextDietaryFiber: EditText
    private lateinit var editTextSodium: EditText
    private lateinit var editTextCaloriesKcal: EditText
    private lateinit var addFoodFormButton: Button
    private lateinit var removeFoodFormButton: Button
    private lateinit var foodNutritionList: List<Food>
    private lateinit var currentFood: Food
    private var jsonUtil = JSON()
    private var cache = Cache()
    private var foodCache: String = ""
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300
    private var lastClickTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_foods)

        initializeUIComponents()

        val foodID = intent.getStringExtra("foodID")

        loadFoodCacheIfNecessary()

        if (foodID != null) {
            setupForFoodUpdate()
        } else {
            removeFoodFormButton.isVisible = false
        }

        addFoodFormButton.setOnClickListener {
            handleAddOrUpdateFood(foodID)
        }

        removeFoodFormButton.setOnClickListener {
            handleFoodRemoval(foodID)
        }

        setupCaloriesCalculation()
    }

    private fun initializeUIComponents() {
        editTextName = findViewById(R.id.editTextName)
        editTextGrams = findViewById(R.id.editTextGrams)
        editTextProtein = findViewById(R.id.editTextProtein)
        editTextCarbohydrate = findViewById(R.id.editTextCarbohydrate)
        editTextLipids = findViewById(R.id.editTextLipids)
        editTextDietaryFiber = findViewById(R.id.editTextDietaryFiber)
        editTextSodium = findViewById(R.id.editTextSodium)
        editTextCaloriesKcal = findViewById(R.id.editTextCaloriesKcal)
        addFoodFormButton = findViewById(R.id.addFoodFormButton)
        removeFoodFormButton = findViewById(R.id.removeFoodFormButton)
    }

    private fun setupForFoodUpdate() {
        addFoodFormButton.text = getString(R.string.update_nutrition_info)

        try {
            loadFoodData()
            populateFoodForm(currentFood)
        } catch (e: Exception) {
            handleFoodLoadingError(e)
        }
    }


    private fun loadFoodCacheIfNecessary() {
        foodCache = cache.getCache(this, "Alimentos")
        if (foodCache == "NOT_FOUND") {
            val rawFoodData = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            cache.setCache(this, "Alimentos", rawFoodData)
            foodCache = rawFoodData
        }
    }

    private fun loadFoodData() {
        intent.getStringExtra("foodObject")?.let {
            currentFood = jsonUtil.fromJson(it, Food::class.java)
            if (currentFood.foodNumber.contains("web")) {
                editable(false)
            }
        }
    }

    private fun editable (b:Boolean) {
        addFoodFormButton.isVisible = false
        removeFoodFormButton.isVisible = false
        editTextName.isEnabled = b
        editTextGrams.isEnabled = b
        editTextProtein.isEnabled = b
        editTextCarbohydrate.isEnabled = b
        editTextLipids.isEnabled = b
    }

    private fun populateFoodForm(food: Food) {
        editTextName.setText(food.foodDescription)
        editTextGrams.setText(food.grams.toString())
        editTextProtein.setText(food.protein)
        editTextCarbohydrate.setText(food.carbohydrate)
        editTextLipids.setText(food.lipids)
        editTextDietaryFiber.setText(food.dietaryFiber)
        editTextSodium.setText(food.sodium)
        editTextCaloriesKcal.setText(food.energyKcal)
    }

    private fun handleFoodLoadingError(e: Exception) {
        Toast.makeText(this,
            getString(R.string.it_was_not_possible_to_load_the_food), Toast.LENGTH_SHORT).show()
        println("Erro food: $e")
    }

    private fun handleAddOrUpdateFood(foodID: String?) {
        if(Food().apply {
            foodNumber = foodID ?: ""
            foodDescription = editTextName.text.toString().takeIf { it.isNotEmpty() } ?: getString(R.string.food_name)
            grams = editTextGrams.text.toString().toDoubleOrNull() ?: 100.0
            protein = editTextProtein.text.toString().toDoubleOrNullOrZero().toString()
            carbohydrate = editTextCarbohydrate.text.toString().toDoubleOrNullOrZero().toString()
            lipids = editTextLipids.text.toString().toDoubleOrNullOrZero().toString()
            dietaryFiber = editTextDietaryFiber.text.toString().toDoubleOrNullOrZero().toString()
            sodium = editTextSodium.text.toString().toDoubleOrNullOrZero().toString()
            energyKcal = editTextCaloriesKcal.text.toString().toDoubleOrNullOrZero().toString()
            energyKj = formatDoubleNumber((energyKcal.toDouble() / grams * 100.0) * 4.184)
        }.saveFood(this)) {
            finish()
        }
    }

    private fun handleFoodRemoval(foodID: String?) {
        val clickTime = System.currentTimeMillis()
        if (isDoubleClick(clickTime)) {
            if (foodID != null) {
                Food().apply { foodNumber = foodID }.removeFood(this)
                finish()
            } else {
                navigateToDailyCalories()
            }
        } else {
            showDoubleClickWarning()
        }
        lastClickTime = clickTime
    }

    private fun isDoubleClick(clickTime: Long): Boolean {
        return clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA
    }

    private fun navigateToDailyCalories() {
        val intent = Intent(this, dailyCalories::class.java)
        startActivity(intent)
    }

    private fun showDoubleClickWarning() {
        Toast.makeText(this, getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
    }

    private fun setupCaloriesCalculation() {
        calcCalories(listOf(editTextProtein, editTextCarbohydrate, editTextLipids))
    }


    private fun calcCalories(editText:List<EditText>) {
        editText.forEach { e ->
            e.addTextChangedListener {
                val protein = editTextProtein.text.toString()
                val carbohydrate = editTextCarbohydrate.text.toString()
                val lipids = editTextLipids.text.toString()

                if (protein.isNotEmpty() && carbohydrate.isNotEmpty() && lipids.isNotEmpty()) {
                    val proteinValue = protein.toDouble()
                    val carbohydrateValue = carbohydrate.toDouble()
                    val lipidsValue = lipids.toDouble()
                    val calories = (proteinValue * 4 + carbohydrateValue * 4 + lipidsValue * 9)
                    // round to 2 decimal places
                    editTextCaloriesKcal.setText(formatDoubleNumber(calories))
                }
            }
        }
    }

    private fun formatDoubleNumber(value: Double):String {
        return "%.2f".format(value).replace(",", ".")
    }


    private fun String.toDoubleOrNullOrZero(): Double {
        return this.toDoubleOrNull() ?: 0.0
    }



}
