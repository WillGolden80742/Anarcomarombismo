package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON
import java.util.Random

class formFoods : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextGrams: EditText
    private lateinit var editTextProtein: EditText
    private lateinit var editTextCarbohydrate: EditText
    private lateinit var editTextLipids: EditText
    private lateinit var editTextDietaryFiber: EditText
    private lateinit var editTextSodium: EditText
    private lateinit var editTextCaloriesKcal: TextView
    private lateinit var addFoodFormButton: Button
    private lateinit var removeFoodFormButton: Button
    private lateinit var foodNutritionList: List<Food>
    private lateinit var currentFood: Food
    private var jsonUtil = JSON()
    private var cache = Cache()
    private var foodCache: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_foods)

        // Inicializando os componentes
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

        // if has extra, load the food
        if (intent.hasExtra("foodID")) {
            val foodID = intent.getStringExtra("foodID")
            addFoodFormButton.text = getString(R.string.update_nutrition_info)
            try {
                foodCache = cache.getCache(this, "Alimentos")
                if (foodCache != "NOT_FOUND") {
                    foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList()
                } else {
                    // get R.raw.nutritional_table and add to cache
                    foodCache = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
                    cache.setCache(this, "Alimentos", foodCache)
                    foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList()
                }
                currentFood = foodNutritionList.find { it.foodNumber == foodID.toString() }!!
                editTextName.setText(currentFood.foodDescription)
                editTextGrams.setText(currentFood.grams.toString())
                editTextProtein.setText(currentFood.protein)
                editTextCarbohydrate.setText(currentFood.carbohydrate)
                editTextLipids.setText(currentFood.lipids)
                editTextDietaryFiber.setText(currentFood.dietaryFiber)
                editTextSodium.setText(currentFood.sodium)
                editTextCaloriesKcal.setText(currentFood.energyKcal)
            } catch (e: Exception) {
                // Toast para indicar que não foi possível carregar o alimento
                Toast.makeText(this, "Não foi possível carregar o alimento", Toast.LENGTH_SHORT).show()
                System.out.println("Erro food: "+e)
            }
        } else {
            removeFoodFormButton.isVisible = false
            //if hash no cache create a cache to
            foodCache = cache.getCache(this, "Alimentos")
            if (foodCache == "NOT_FOUND") {
                // get R.raw.nutritional_table and add to cache
                foodCache = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
                cache.setCache(this, "Alimentos", foodCache)
            }
        }
        // Listener para o botão de adicionar food
        addFoodFormButton.setOnClickListener {
            if (intent.hasExtra("foodID")) {
                saveFood(1)
            } else {
                saveFood(0)
            }
        }
        // Listener para o botão de remove food
        removeFoodFormButton.setOnClickListener {
            if (intent.hasExtra("foodID")) {
                removeFood()
            } else {
                val intent = Intent(this, dailyCalories::class.java)
                startActivity(intent)
            }
        }

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

    fun formatDoubleNumber(value: Double):String {
        return "%.2f".format(value).replace(",", ".")
    }


    fun removeFood() {
        foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().filter { it.foodNumber != currentFood.foodNumber }
        cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
        finish()
        Toast.makeText(this, getString(R.string.alimento_removido_com_sucesso), Toast.LENGTH_SHORT).show()
    }

    // edit food
    fun saveFood(action: Int) {
        try {
            val foodDescription = editTextName.text.toString()
            var grams = editTextGrams.text.toString().toDoubleOrNull() ?: 0.0
            val protein = editTextProtein.text.toString()
            val carbohydrate = editTextCarbohydrate.text.toString()
            val lipids = editTextLipids.text.toString()
            val dietaryFiber = editTextDietaryFiber.text.toString()
            val sodium = editTextSodium.text.toString()
            val calorieskcal = editTextCaloriesKcal.text.toString()

            // Verificar se todos os campos estão preenchidos
            if (foodDescription.isEmpty() || grams == 0.0 || protein.isEmpty() || carbohydrate.isEmpty() || lipids.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.todos_os_campos_s_o_obrigat_rios),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val food = Food().apply {
                if (action == 1) { // Atualizar
                    foodNumber = currentFood.foodNumber
                } else { // Criar
                    val random = Random().nextInt(100)
                    foodNumber = (System.currentTimeMillis()).toString()+random
                }
                if (foodDescription.isEmpty()) {
                    this.foodDescription = getString(R.string.nome_de_comida)
                } else {
                    this.foodDescription = foodDescription
                }
                if (grams == 0.0) {
                    grams = 100.0
                }
                this.grams = 100.0
                this.protein = formatDoubleNumber((protein.toDouble() / grams * 100))
                this.carbohydrate = formatDoubleNumber((carbohydrate.toDouble() / grams * 100))
                this.lipids = formatDoubleNumber((lipids.toDouble() / grams * 100))
                this.dietaryFiber = formatDoubleNumber((dietaryFiber.toDouble() / grams * 100))
                this.sodium = formatDoubleNumber((sodium.toDouble() / grams * 100))
                this.energyKcal = formatDoubleNumber((calorieskcal.toDouble() / grams * 100))
                this.energyKj = formatDoubleNumber(((calorieskcal.toDouble() / grams * 100) * 4.184))
            }

            if (action == 1) { // Atualizar
                foodNutritionList =
                    jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().map {
                        if (it.foodNumber == currentFood.foodNumber) {
                            food
                        } else {
                            it
                        }
                    }
                Toast.makeText(
                    this,
                    getString(R.string.update_nutrition_sucessful),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (action == 0) { // Criar
                foodNutritionList =
                    jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList() + food
                // Concluir e enviar a lista de alimentos para a próxima atividade
                val intent = Intent(this, dailyCalories::class.java)
                startActivity(intent)
                Toast.makeText(
                    this,
                    getString(R.string.alimento_salvo_com_sucesso),
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Salvar no cache
            cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar o alimento", Toast.LENGTH_SHORT).show()
        }
    }


}
