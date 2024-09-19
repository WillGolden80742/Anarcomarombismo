package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.util.UUID

class Food (
    var foodNumber: String = "",
    var grams: Double = 100.0,
    var foodDescription: String = "NO_DESCRIPTION",
    var moisture: String = "0.0",
    var energyKcal: String = "0.0",
    var energyKj: String = "0.0",
    var protein: String = "0.0",
    var lipids: String = "0.0",
    var cholesterol: String = "0.0",
    var carbohydrate: String = "0.0",
    var dietaryFiber: String = "0.0",
    var ash: String = "0.0",
    var calcium: String = "0.0",
    var magnesium: String = "0.0",
    var manganese: String = "0.0",
    var phosphorus: String = "0.0",
    var iron: String = "0.0",
    var sodium: String = "0.0",
    var potassium: String = "0.0",
    var copper: String = "0.0",
    var zinc: String = "0.0",
    var retinol: String = "0.0",
    var re: String = "0.0",
    var rae: String = "0.0",
    var thiamine: String = "0.0",
    var riboflavin: String = "0.0",
    var pyridoxine: String = "0.0",
    var niacin: String = "0.0",
    var vitaminC: String = ""
) {
    init {
        if (grams == null) {
            grams = 100.00
        }
    }
    companion object {
        private var json = JSON()
        private var cache = Cache()
        fun build(
            foodNumber: String = "",
            grams: Double = 100.0,
            foodDescription: String,
            protein: String,
            carbohydrate: String,
            lipids: String,
            dietaryFiber: String,
            sodium: String,
            energyKcal: String
        ): Food {
            return Food().apply {
                this.foodNumber = foodNumber.ifEmpty { generateFoodNumber() }
                this.foodDescription = foodDescription
                this.protein = normalizeNutrient(protein, grams)
                this.carbohydrate = normalizeNutrient(carbohydrate,grams)
                this.lipids = normalizeNutrient(lipids,grams)
                this.dietaryFiber = normalizeNutrient(dietaryFiber,grams)
                this.sodium = normalizeNutrient(sodium,grams)
                this.energyKcal = normalizeNutrient(energyKcal,grams)
                this.energyKj = formatDoubleNumber((energyKcal.toDouble() / grams * 100.0) * 4.184)
                this.grams = 100.0
            }
        }
    }
    fun save(context: Context): Boolean {
        return save(this, context)
    }
    private fun save(food: Food, context: Context): Boolean {
        try {
            val foodCache = loadJSONCache(context)

            if (food.foodDescription == context.getString(R.string.food_name)) {
                showToast(context.getString(R.string.fill_in_the_food_name), context)
                return false
            }

            val isUpdate = food.foodNumber.isNotEmpty() && foodCache.contains(food.foodNumber)

            val foodNutritionList = if (isUpdate) {
                updateFoodInList(food, foodCache)
            } else {
                createFoodInList(food, foodCache)
            }

            showToast(
                if (isUpdate) context.getString(R.string.update_nutrition_sucessful)
                else context.getString(R.string.successful_target_food),
                context
            )
            cache.setCache(context, "Alimentos", json.toJson(foodNutritionList))
            return true

        } catch (e: Exception) {
            showToast(context.getString(R.string.save_food_error), context)
            return false
        }
    }

    fun loadList(context: Context): List<Food> {
        return json.fromJson(loadJSONCache(context), Array<Food>::class.java).toList()
    }
    fun remove(context: Context) {
        try {
            val foodNutritionList = json.fromJson(loadJSONCache(context), Array<Food>::class.java).toList()
                .filter { it.foodNumber != foodNumber }
            cache.setCache(context, "Alimentos", json.toJson(foodNutritionList))
            Toast.makeText(context, context.getString(R.string.successfully_removed_food), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_removing_food), Toast.LENGTH_SHORT).show()
            println("Erro food: $e")
        }
    }

    private fun loadJSONCache (context: Context): String {
        var foodCache: String
        if (cache.hasCache(context, "Alimentos")) {
            foodCache = cache.getCache(context, "Alimentos")
        } else {
            val rawFoodData = context.resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            cache.setCache(context, "Alimentos", rawFoodData)
            foodCache = rawFoodData
        }
        return foodCache
    }

    private fun updateFoodInList(food: Food, foodCache: String): List<Food> {
        return json.fromJson(foodCache, Array<Food>::class.java).toList()
            .map { if (it.foodNumber == food.foodNumber) food else it }
    }

    private fun createFoodInList(food: Food, foodCache: String): List<Food> {
        return json.fromJson(foodCache, Array<Food>::class.java).toList() + food
    }

    private fun showToast(message: String,context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun normalizeNutrient(nutrient: String, grams: Double): String {
        return formatDoubleNumber(nutrient.toDouble() / grams * 100.0)
    }

    private fun formatDoubleNumber(value: Double):String {
        return "%.2f".format(value).replace(",", ".")
    }

    private fun generateFoodNumber(): String {
        val random = UUID.randomUUID().toString()
        return "${System.currentTimeMillis()}$random"
    }


    fun toString(context: Context): String {
        val decimalFormat = DecimalFormat("#.##")
        val gramsLabel = context.getString(R.string.grams)
        val energyKcalLabel = context.getString(R.string.energy_kcal)
        val energyKjLabel = context.getString(R.string.energy_kj)
        val proteinLabel = context.getString(R.string.protein)
        val lipidsLabel = context.getString(R.string.lipids)
        val carbohydrateLabel = context.getString(R.string.carbohydrate)
        val dietaryFiberLabel = context.getString(R.string.dietary_fiber)
        val sodiumLabel = context.getString(R.string.sodium)

        return if (grams > 0.0) {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
            energyKcalLabel + " : " + decimalFormat.format((energyKcal!!.toDouble() * grams) / 100.0) + ",\n" +
            energyKjLabel + " : " + decimalFormat.format((energyKj!!.toDouble() * grams) / 100.0) + ",\n" +
            proteinLabel + " : " + decimalFormat.format((protein!!.toDouble() * grams) / 100.0) + ",\n" +
            lipidsLabel + " : " + decimalFormat.format((lipids!!.toDouble() * grams) / 100.0) + ",\n" +
            carbohydrateLabel + " : " + decimalFormat.format((carbohydrate!!.toDouble() * grams) / 100.0) + ",\n" +
            dietaryFiberLabel + " : " + decimalFormat.format((dietaryFiber!!.toDouble() * grams) / 100.0) + ",\n" +
            sodiumLabel + " : " + decimalFormat.format((sodium!!.toDouble() * grams) / 100.0)
        } else {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
            energyKcalLabel + " : " + decimalFormat.format(energyKcal!!.toDouble()) + ",\n" +
            energyKjLabel + " : " + decimalFormat.format(energyKj!!.toDouble()) + ",\n" +
            proteinLabel + " : " + decimalFormat.format(protein!!.toDouble()) + ",\n" +
            lipidsLabel + " : " + decimalFormat.format(lipids!!.toDouble()) + ",\n" +
            carbohydrateLabel + " : " + decimalFormat.format(carbohydrate!!.toDouble()) + ",\n" +
            dietaryFiberLabel + " : " + decimalFormat.format(dietaryFiber!!.toDouble()) + ",\n" +
            sodiumLabel + " : " + decimalFormat.format(sodium!!.toDouble())
        }
    }
}