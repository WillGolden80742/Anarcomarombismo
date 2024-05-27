package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.Controller.JSON

class formFoods : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextGrams: EditText
    private lateinit var editTextProtein: EditText
    private lateinit var editTextCarbohydrate: EditText
    private lateinit var editTextLipids: EditText
    private lateinit var editTextCholesterol: EditText
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_foods)

        // Inicializando os componentes
        editTextName = findViewById(R.id.editTextName)
        editTextGrams = findViewById(R.id.editTextGrams)
        editTextProtein = findViewById(R.id.editTextProtein)
        editTextCarbohydrate = findViewById(R.id.editTextCarbohydrate)
        editTextLipids = findViewById(R.id.editTextLipids)
        editTextCholesterol = findViewById(R.id.editTextCholesterol)
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
                editTextCholesterol.setText(currentFood.cholesterol)
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
                updateFood()
            } else {
                createFood()
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
    }

    fun createFood() {
        val foodDescription = editTextName.text.toString()
        var grams = editTextGrams.text.toString().toDouble()
        val protein = editTextProtein.text.toString()
        val carbohydrate = editTextCarbohydrate.text.toString()
        val lipids = editTextLipids.text.toString()
        val cholesterol = editTextCholesterol.text.toString()
        val dietaryFiber = editTextDietaryFiber.text.toString()
        val sodium = editTextSodium.text.toString()
        val calorieskcal = editTextCaloriesKcal.text.toString()

        // Verificar se todos os campos estão preenchidos
        if (foodDescription.isEmpty() || grams == 0.0 || protein.isEmpty() || carbohydrate.isEmpty() || lipids.isEmpty() ||
            dietaryFiber.isEmpty() || sodium.isEmpty() || calorieskcal.isEmpty()) {
            // Mostrar mensagem de erro indicando qual campo está vazio
            Toast.makeText(this, "Todos os campos são obrigatórios", Toast.LENGTH_SHORT).show()
            return // Impede a execução do código restante se algum campo estiver vazio
        }

        val food = Food().apply {
            foodNumber = (System.currentTimeMillis()).toString()
            if (foodDescription.isEmpty()) {
                this.foodDescription = getString(R.string.nome_de_comida)
            } else {
                this.foodDescription = foodDescription
            }
            if (grams == 0.0) {
                grams = 100.0
            }
            this.grams = 100.0
            this.protein = (protein.toDouble() / grams * 100).toString()
            this.carbohydrate = (carbohydrate.toDouble() / grams * 100).toString()
            this.lipids = (lipids.toDouble() / grams * 100).toString()
            this.cholesterol = (cholesterol.toDouble() / grams * 100).toString()
            this.dietaryFiber = (dietaryFiber.toDouble() / grams * 100).toString()
            this.sodium = (sodium.toDouble() / grams * 100).toString()
            this.energyKcal = (calorieskcal.toDouble() / grams * 100).toString()
            // Converter calorias para kJ
            this.energyKj = ((calorieskcal.toDouble() / grams * 100) * 4.184).toString()
        }

        // Adicionar alimento à lista e salvar no cache
        foodNutritionList =  jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList() + food
        cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))

        // Concluir e enviar a lista de alimentos para a próxima atividade
        val intent = Intent(this, dailyCalories::class.java)
        startActivity(intent)
        Toast.makeText(this, getString(R.string.alimento_salvo_com_sucesso), Toast.LENGTH_SHORT).show()
    }

    fun removeFood() {
        foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().filter { it.foodNumber != currentFood.foodNumber }
        cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
        finish()
        Toast.makeText(this, getString(R.string.alimento_removido_com_sucesso), Toast.LENGTH_SHORT).show()
    }

    // edit food
    fun updateFood() {
        val foodDescription = editTextName.text.toString()
        var grams = editTextGrams.text.toString().toDouble()
        val protein = editTextProtein.text.toString()
        val carbohydrate = editTextCarbohydrate.text.toString()
        val lipids = editTextLipids.text.toString()
        val cholesterol = editTextCholesterol.text.toString()
        val dietaryFiber = editTextDietaryFiber.text.toString()
        val sodium = editTextSodium.text.toString()
        val calorieskcal = editTextCaloriesKcal.text.toString()

        // Verificar se todos os campos estão preenchidos val foodDescription = editTextName.text.toString() var grams = editTextGrams.text.toString().toDouble() val protein = editTextProtein.text.toString() val carbohydrate = editTextCarbohydrate.text.toString() val lipids = editTextLipids.text.toString() val cholesterol = editTextCholesterol.text.toString() val dietaryFiber = editTextDietaryFiber.text.toString() val sodium = editTextSodium.text.toString() val calorieskcal = editTextCaloriesKcal.text.toString()
        if (foodDescription.isEmpty() || grams == 0.0 || protein.isEmpty() || carbohydrate.isEmpty() || lipids.isEmpty() ||
            dietaryFiber.isEmpty() || sodium.isEmpty() || calorieskcal.isEmpty()
            ) {
            // Mostrar mensagem de erro indicando qual campo está vazio
            Toast.makeText(this, getString(R.string.todos_os_campos_s_o_obrigat_rios), Toast.LENGTH_SHORT).show()
            return // Impede a execução do código restante se algum campo estiver vazio
        }

        val food = Food().apply {
            foodNumber = currentFood.foodNumber
            if (foodDescription.isEmpty()) {
                this.foodDescription = getString(R.string.nome_de_comida)
            } else {
                this.foodDescription = foodDescription
            }
            if (grams == 0.0) {
                grams = 100.0
            }
            this.grams = 100.0
            this.protein = (protein.toDouble() / grams * 100).toString()
            this.carbohydrate = (carbohydrate.toDouble() / grams * 100).toString()
            this.lipids = (lipids.toDouble() / grams * 100).toString()
            this.cholesterol = (cholesterol.toDouble() / grams * 100).toString()
            this.dietaryFiber = (dietaryFiber.toDouble() / grams * 100).toString()
            this.sodium = (sodium.toDouble() / grams * 100).toString()
            this.energyKcal = (calorieskcal.toDouble() / grams * 100).toString()
            // Converter calorias para kJ
            this.energyKj = ((calorieskcal.toDouble() / grams * 100) * 4.184).toString()
        }

        // Adicionar alimento à lista e salvar no cache
        foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().map {
                if (it.foodNumber == currentFood.foodNumber) {
                    food
                } else {
                    it
                }
        }

        cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
        finish()
        Toast.makeText(this, getString(R.string.update_nutrition_sucessful), Toast.LENGTH_SHORT).show()
    }

}
