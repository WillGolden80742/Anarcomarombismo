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

        if (foodID != null) {
            setupForFoodUpdate()
        } else {
            prepareForNewFoodEntry()
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

    private fun prepareForNewFoodEntry() {
        removeFoodFormButton.isVisible = false
        loadFoodCacheIfNecessary()
    }

    private fun loadFoodCacheIfNecessary() {
        foodCache = cache.getCache(this, "Alimentos")
        if (foodCache == "NOT_FOUND") {
            val rawFoodData = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            cache.setCache(this, "Alimentos", rawFoodData)
        }
    }

    private fun loadFoodData() {
        intent.getStringExtra("foodObject")?.let {
            currentFood = jsonUtil.fromJson(it, Food::class.java)
        }
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
        if (foodID != null) {
            saveFood(1)
        } else {
            saveFood( 0)
        }
    }

    private fun handleFoodRemoval(foodID: String?) {
        val clickTime = System.currentTimeMillis()
        if (isDoubleClick(clickTime)) {
            if (foodID != null) {
                removeFood()
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


    private fun removeFood() {
        try {
            foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList()
                .filter { it.foodNumber != currentFood.foodNumber }
            cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
            Toast.makeText(this, getString(R.string.successfully_removed_food), Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_removing_food), Toast.LENGTH_SHORT).show()
            println("Erro food: $e")
        }
        finish()
    }

    // edit food
    private fun saveFood(action: Int) {
        try {
            val foodDescription = editTextName.text.toString().takeIf { it.isNotEmpty() }
                ?: getString(R.string.food_name)
            val grams = editTextGrams.text.toString().toDoubleOrNull() ?: 100.0
            val protein = editTextProtein.text.toString().toDoubleOrNullOrZero()
            val carbohydrate = editTextCarbohydrate.text.toString().toDoubleOrNullOrZero()
            val lipids = editTextLipids.text.toString().toDoubleOrNullOrZero()
            val dietaryFiber = editTextDietaryFiber.text.toString().toDoubleOrNullOrZero()
            val sodium = editTextSodium.text.toString().toDoubleOrNullOrZero()
            val caloriesKcal = editTextCaloriesKcal.text.toString().toDoubleOrNullOrZero()

            if (foodDescription == getString(R.string.food_name)) {
                showToast(getString(R.string.fill_in_the_food_name))
                return
            }

            val food = createFood(
                foodDescription = foodDescription,
                grams = grams,
                protein = protein,
                carbohydrate = carbohydrate,
                lipids = lipids,
                dietaryFiber = dietaryFiber,
                sodium = sodium,
                caloriesKcal = caloriesKcal,
                action = action
            )

            updateOrCreateFood(action, food)
            cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
            finish()
        } catch (e: Exception) {
            showToast(getString(R.string.save_food_error))
        }
    }

    private fun String.toDoubleOrNullOrZero(): Double {
        return this.toDoubleOrNull() ?: 0.0
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createFood(
        foodDescription: String,
        grams: Double,
        protein: Double,
        carbohydrate: Double,
        lipids: Double,
        dietaryFiber: Double,
        sodium: Double,
        caloriesKcal: Double,
        action: Int
    ): Food {
        return Food().apply {
            foodNumber = if (action == 1) currentFood.foodNumber else generateFoodNumber()
            this.foodDescription = foodDescription
            this.grams = 100.0
            this.protein = formatDoubleNumber((protein / grams * 100.0))
            this.carbohydrate = formatDoubleNumber((carbohydrate / grams * 100.0))
            this.lipids = formatDoubleNumber((lipids / grams * 100.0))
            this.dietaryFiber = formatDoubleNumber((dietaryFiber / grams * 100.0))
            this.sodium = formatDoubleNumber((sodium / grams * 100.0))
            this.energyKcal = formatDoubleNumber((caloriesKcal / grams * 100.0))
            this.energyKj = formatDoubleNumber((caloriesKcal / grams * 100.0) * 4.184)
        }
    }

    private fun generateFoodNumber(): String {
        val random = UUID.randomUUID().toString()
        return "${System.currentTimeMillis()}$random"
    }

    private fun updateOrCreateFood(action: Int, food: Food) {
        foodNutritionList = when (action) {
            1 -> updateFoodInList(food)
            0 -> createFoodInList(food)
            else -> foodNutritionList
        }
        showToast(
            if (action == 1) getString(R.string.update_nutrition_sucessful)
            else getString(R.string.successful_target_food)
        )
    }

    private fun updateFoodInList(food: Food): List<Food> {
        return jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().map {
            if (it.foodNumber == currentFood.foodNumber) food else it
        }
    }

    private fun createFoodInList(food: Food): List<Food> {
        return jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList() + food
    }



}
