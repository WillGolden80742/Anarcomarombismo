package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.NumberFormatter
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.util.UUID

class Food(
    var foodNumber: String = "",
    var grams: Double = 100.0,
    var foodDescription: String = "NO_DESCRIPTION",
    var energyKcal: String = "0.0",
    var energyKj: String = "0.0",
    var protein: String = "0.0",
    var lipids: String = "0.0",
    var cholesterol: String = "0.0",
    var carbohydrate: String = "0.0",
    var dietaryFiber: String = "0.0",
    var sodium: String = "0.0",
): DataHandler<Food> {
    companion object {
        private var cache = Cache()

        fun build(
            foodNumber: String = "",
            grams: Double = 100.0,
            foodDescription: String = "NO_DESCRIPTION",
            protein: String = "0.0",
            carbohydrate: String = "0.0",
            lipids: String = "0.0",
            dietaryFiber: String = "0.0",
            sodium: String = "0.0",
            energyKcal: String = "0.0"
        ): Food {
            return Food().apply {
                this.foodNumber = foodNumber.ifEmpty { generateFoodNumber() }
                this.foodDescription = foodDescription
                this.protein = normalizeNutrient(protein, grams)
                this.carbohydrate = normalizeNutrient(carbohydrate, grams)
                this.lipids = normalizeNutrient(lipids, grams)
                this.dietaryFiber = normalizeNutrient(dietaryFiber, grams)
                this.sodium = normalizeNutrient(sodium, grams)
                this.energyKcal = normalizeNutrient(energyKcal, grams)
                this.energyKj = NumberFormatter.formatDoubleNumber((energyKcal.toDouble() / grams * 100.0) * 4.184)
                this.grams = 100.0
            }
        }
    }

    override fun save(context: Context): Boolean {
        return save(this, context)
    }

    private fun save(food: Food, context: Context): Boolean {
        val contextualKey = context.getString(R.string.foods)
        try {
            val foodCache = fetchAll(context)

            if (food.foodDescription == context.getString(R.string.food_name)) {
                showToast(context.getString(R.string.fill_in_the_food_name), context)
                return false
            }

            val isUpdate = food.foodNumber.isNotEmpty() && foodCache.any { it.foodNumber == food.foodNumber }

            val foodNutritionList = if (isUpdate) {
                foodCache.map { if (it.foodNumber == food.foodNumber) food else it }
            } else {
                foodCache + food
            }

            showToast(
                if (isUpdate) context.getString(R.string.update_nutrition_successful)
                else context.getString(R.string.successful_target_food),
                context
            )
            cache.setCache(context, contextualKey, foodNutritionList)
            return true

        } catch (e: Exception) {
            showToast(context.getString(R.string.save_food_error), context)
            return false
        }
    }

    override fun fetchAll(context: Context): List<Food> {
        val contextualKey = context.getString(R.string.foods)
        return if (cache.hasCache(context, contextualKey)) {
            cache.getCache(context, contextualKey, Array<Food>::class.java).toList()
        } else {
            val rawFoodData = context.resources.openRawResource(R.raw.nutritional_table).bufferedReader().use { it.readText() }
            val foodList = JSON.fromJson(rawFoodData, Array<Food>::class.java).toList()
            cache.setCache(context, contextualKey, foodList)
            foodList
        }
    }

    override fun fetchById(context: Context, id: Any): Food {
        return fetchAll(context).first { it.foodNumber == id as String }
    }

    override fun remove(context: Context): Boolean {
        val contextualKey = context.getString(R.string.foods)
        try {
            val foodNutritionList = fetchAll(context).filter { it.foodNumber != foodNumber }
            cache.setCache(context, contextualKey, foodNutritionList)
            showToast(context.getString(R.string.successfully_removed_food), context)
            return true
        } catch (e: Exception) {
            showToast(context.getString(R.string.error_removing_food), context)
            println("Erro food: $e")
            return false
        }
    }

    private fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun normalizeNutrient(nutrient: String, grams: Double): String {
        return NumberFormatter.formatDoubleNumber(nutrient.toDouble() / grams * 100.0)
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
        val proteinLabel = context.getString(R.string.proteins)
        val lipidsLabel = context.getString(R.string.lipids)
        val carbohydrateLabel = context.getString(R.string.carbohydrates)
        val dietaryFiberLabel = context.getString(R.string.dietary_fibers)
        val sodiumLabel = context.getString(R.string.sodium)

        return if (grams > 0.0) {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
                    energyKcalLabel + " : " + decimalFormat.format((energyKcal.toDouble() * grams) / 100.0) + ",\n" +
                    energyKjLabel + " : " + decimalFormat.format((energyKj.toDouble() * grams) / 100.0) + ",\n" +
                    proteinLabel + " : " + decimalFormat.format((protein.toDouble() * grams) / 100.0) + ",\n" +
                    lipidsLabel + " : " + decimalFormat.format((lipids.toDouble() * grams) / 100.0) + ",\n" +
                    carbohydrateLabel + " : " + decimalFormat.format((carbohydrate.toDouble() * grams) / 100.0) + ",\n" +
                    dietaryFiberLabel + " : " + decimalFormat.format((dietaryFiber.toDouble() * grams) / 100.0) + ",\n" +
                    sodiumLabel + " : " + decimalFormat.format((sodium.toDouble() * grams) / 100.0)
        } else {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
                    energyKcalLabel + " : " + decimalFormat.format(energyKcal.toDouble()) + ",\n" +
                    energyKjLabel + " : " + decimalFormat.format(energyKj.toDouble()) + ",\n" +
                    proteinLabel + " : " + decimalFormat.format(protein.toDouble()) + ",\n" +
                    lipidsLabel + " : " + decimalFormat.format(lipids.toDouble()) + ",\n" +
                    carbohydrateLabel + " : " + decimalFormat.format(carbohydrate.toDouble()) + ",\n" +
                    dietaryFiberLabel + " : " + decimalFormat.format(dietaryFiber.toDouble()) + ",\n" +
                    sodiumLabel + " : " + decimalFormat.format(sodium.toDouble())
        }
    }
}
