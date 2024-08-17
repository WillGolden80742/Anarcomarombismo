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
import com.example.anarcomarombismo.Controller.FoodSearch
import com.example.anarcomarombismo.Controller.JSON
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URLEncoder
import java.text.DecimalFormat
import java.util.UUID
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

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
            setupForFoodUpdate(foodID)
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

        // fetchFoodDataAsync("feijão")

        // fetchSelecteFoodAsync("https://www.fatsecret.com.br/calorias-nutri%C3%A7%C3%A3o/gen%C3%A9rico/feij%C3%A3o-carioca-cozido")
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

    private fun setupForFoodUpdate(foodID: String) {
        addFoodFormButton.text = getString(R.string.update_nutrition_info)

        try {
            loadFoodData(foodID)
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

    private fun loadFoodData(foodID: String) {
        foodCache = cache.getCache(this, "Alimentos")
        foodNutritionList = if (foodCache != "NOT_FOUND") {
            jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList()
        } else {
            val rawFoodData = resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            cache.setCache(this, "Alimentos", rawFoodData)
            jsonUtil.fromJson(rawFoodData, Array<Food>::class.java).toList()
        }
        currentFood = foodNutritionList.find { it.foodNumber == foodID }
            ?: throw Exception("Food with ID $foodID not found")
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
        foodNutritionList = jsonUtil.fromJson(foodCache, Array<Food>::class.java).toList().filter { it.foodNumber != currentFood.foodNumber }
        cache.setCache(this, "Alimentos", jsonUtil.toJson(foodNutritionList))
        finish()
        Toast.makeText(this, getString(R.string.successfully_removed_food), Toast.LENGTH_SHORT).show()
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
            this.protein = formatDoubleNumber((protein / grams * 100))
            this.carbohydrate = formatDoubleNumber((carbohydrate / grams * 100))
            this.lipids = formatDoubleNumber((lipids / grams * 100))
            this.dietaryFiber = formatDoubleNumber((dietaryFiber / grams * 100))
            this.sodium = formatDoubleNumber((sodium / grams * 100))
            this.energyKcal = formatDoubleNumber((caloriesKcal / grams * 100))
            this.energyKj = formatDoubleNumber((caloriesKcal / grams * 100) * 4.184)
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

    fun fetchFoodData(query: String): List<FoodSearch> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://www.fatsecret.com.br/calorias-nutri%C3%A7%C3%A3o/search?q=$encodedQuery"
        val items = mutableListOf<FoodSearch>()

        try {
            val document: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()

            val links: Elements = document.select("a.prominent")
            val smallTextDivs: Elements = document.select("div.smallText")

            for (link in links) {
                val href = link.attr("href")
                val name = link.text().trim()
                var smallTextContent = ""

                for (div in smallTextDivs) {
                    if (div.parent() == link.parent()) {
                        smallTextContent = div.text().trim()
                        break
                    }
                }

                val smallTextBeforeDash = smallTextContent.split("-")[0]

                val grams: String? = Regex("(\\d+\\s*g|\\d+g)").find(smallTextBeforeDash)?.value

                if (grams != null) {
                    items.add(
                        FoodSearch(
                            name = name,
                            href = "https://www.fatsecret.com.br$href",
                            smallText = smallTextBeforeDash,
                            grams = grams.replace("g", "")
                        )
                    )
                }
            }
        } catch (e: IOException) {
            println("Erro: ${e.message}")
            // Retorna uma lista vazia em caso de erro
            return emptyList()
        }
        return items
    }

    fun parseFoodData(url: String): Food {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) throw Exception("Failed to fetch data")

        val html = response.body?.string() ?: throw Exception("No content received")
        val doc: Document = Jsoup.parse(html)

        // Extract food description
        val foodDescription = doc.select("h1[style='text-transform:none']").text().trim()

        // Extract nutrients
        val nutrients = mutableMapOf<String, String>()
        doc.select("div.nutrient.left").forEach { element: Element ->
            val keyText = element.text().trim()
            val value = element.nextElementSibling()?.text()?.replace(',', '.')?.trim() ?: "0"
            nutrients[keyText] = value
        }

        // Convert Kj to Kcal
        fun convertKjToKcal(kj: Double): Double = kj / 4.184

        // Extract energy and convert
        val energyKj = nutrients["Energia"]?.replace(Regex("[^0-9,.]"), "")?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
        val energyKcal = convertKjToKcal(energyKj)

        // Generate a foodNumber with a random number and timestamp
        val foodNumber = "${Random.nextInt(1000, 9999)}${System.currentTimeMillis()}"

        // Create the Food object
        return Food(
            foodNumber = foodNumber,
            foodDescription = foodDescription,
            energyKcal = DecimalFormat("#.##").format(energyKcal).replace(",", "."),
            energyKj = (energyKj * 100).toString(),
            protein = nutrients["Proteínas"]?.replace(Regex("[^0-9.]"), "") ?: "0.0",
            lipids = nutrients["Gorduras"]?.replace(Regex("[^0-9.]"), "") ?: "0.0",
            cholesterol = nutrients["Colesterol"]?.replace(Regex("[^0-9]"), "") ?: "0.0",
            carbohydrate = nutrients["Carboidratos"]?.replace(Regex("[^0-9.]"), "") ?: "0.0",
            dietaryFiber = nutrients["Fibras"]?.replace(Regex("[^0-9.]"), "") ?: "0.0",
            sodium = nutrients["Sódio"]?.replace(Regex("[^0-9]"), "") ?: "0.0",
            potassium = nutrients["Potássio"]?.replace(Regex("[^0-9]"), "") ?: "0.0"
        )
    }

    fun parseNutritionalData(html: String) {
        // Parse o HTML
        val doc: Document = Jsoup.parse(html)

        // Selecione a tabela de fatos nutricionais
        val nutritionFactsDiv: Element? = doc.select("div.nutrition_facts").first()

        // Obtenha a lista de nutrientes
        val nutrients: Elements = nutritionFactsDiv!!.select("div.nutrient")

        // Mapeie os nutrientes
        val data = mutableMapOf<String, String>()
        var nutrientName = ""
        for (nutrient in nutrients) {
            val text = nutrient.text()
            if (nutrient.hasClass("left")) {
                nutrientName = text
            } else if (nutrient.hasClass("right")) {
                data[nutrientName] = text
            }
        }

        // Exiba os dados
        data.forEach { (key, value) ->
            println("$key: $value")
        }
    }

    // Usar Coroutine para chamada assíncrona
    fun fetchFoodDataAsync(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = fetchFoodData(query)
            var jsonUtil = JSON()
            withContext(Dispatchers.Main) {
                for (foodSearch in result) {
                    println("${jsonUtil.toJson(foodSearch)}")
                }
            }
        }
    }

    fun fetchSelecteFoodAsync (url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = parseFoodData(url)
            var jsonUtil = JSON()
            withContext(Dispatchers.Main) {
                println("${jsonUtil.toJson(result)}")
            }
        }
    }

}
