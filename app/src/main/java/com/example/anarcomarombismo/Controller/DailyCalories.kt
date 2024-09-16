package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyCalories {
    var date: String = ""
    var protein: Double = 0.0
    var carbohydrate: Double = 0.0
    var lipids: Double = 0.0
    var cholesterol: Double = 0.0
    var dietaryFiber: Double = 0.0
    var sodium: Double = 0.0
    var calorieskcal: Double = 0.0
    var calorieskj: Double = 0.0
    var foodsList: List<Food> = listOf()

    init {
        if (date == "") {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            date = formattedDate
        }
    }

    fun save(context: Context): Boolean {
        val cache = Cache()
        val jsonUtil = JSON()
        return try {
            // Obtém a lista existente de DailyCalories do cache
            val dailyCaloriesList = getExistingDailyCaloriesList(context, cache, jsonUtil)

            // Filtra a lista para remover qualquer item com a mesma data
            val updatedCaloriesList = dailyCaloriesList.filterNot { it.date == this.date } + this

            // Salva a lista atualizada no cache
            cache.setCache(context, "dailyCalories", jsonUtil.toJson(updatedCaloriesList))
            true
        } catch (e: Exception) {
            handleError(e, "Error saving daily calories")
            false
        }
    }

    fun remove(context: Context):Boolean {
        val currentDate = date
        val cacheKey = "dailyCalories"
        val cache = Cache()

        cache.getCache(context, cacheKey)?.let { dailyCaloriesListJson ->
            val dailyCaloriesList = parseDailyCaloriesList(dailyCaloriesListJson)
            val updatedCaloriesList = removeCaloriesForDate(dailyCaloriesList, currentDate)

            if (updatedCaloriesList.size != dailyCaloriesList.size) {
                cache.setCache(context, cacheKey, toJson(updatedCaloriesList))
                showToast(context, R.string.daily_calories_removed_successfully)
                resetDailyCalories(currentDate)
            }
        }
        return true
    }

    fun load(context: Context, selectedDate: String, callback: (DailyCalories) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            if (cache.hasCache(context, "dailyCalories")) {
                val dailyCaloriesListJson = cache.getCache(context, "dailyCalories")
                val jsonUtil = JSON()
                val dailyCaloriesList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
                val dailyCaloriesListFiltered = dailyCaloriesList.filter { it.date == selectedDate }
                withContext(Dispatchers.Main) {
                    if (dailyCaloriesListFiltered.isNotEmpty()) {
                        callback(dailyCaloriesListFiltered[0])
                    } else {
                        val newDailyCalories = DailyCalories().apply { date = selectedDate }
                        callback(newDailyCalories)
                    }
                }
            }
        }
    }
    private fun getExistingDailyCaloriesList(context: Context, cache: Cache, jsonUtil: JSON): List<DailyCalories> {
        return if (cache.hasCache(context, "dailyCalories")) {
            val dailyCaloriesListJson = cache.getCache(context, "dailyCalories")
            jsonUtil.fromJsonArray(dailyCaloriesListJson)
        } else {
            emptyList()
        }
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

    private fun showToast(context: Context, messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    private fun resetDailyCalories(date: String) {
        this.date = date
        this.foodsList = listOf() // Reset food list if needed
    }

    private fun handleError(e: Exception, message: String) {
        println(RuntimeException("$message: $e"))
    }
    fun toString(context: Context): String {
        val decimalFormat = DecimalFormat("#.##")
        val energyKcalLabel = context.getString(R.string.energy_kcal)
        val energyKjLabel = context.getString(R.string.energy_kj)
        val proteinLabel = context.getString(R.string.protein)
        val lipidsLabel = context.getString(R.string.lipids)
        val carbohydrateLabel = context.getString(R.string.carbohydrate)
        val dietaryFiberLabel = context.getString(R.string.dietary_fiber)
        val sodiumLabel = context.getString(R.string.sodium)

        return  energyKcalLabel + " : " + decimalFormat.format(calorieskcal) + ", \n" +
                energyKjLabel + " : " + decimalFormat.format(calorieskj) + ", \n" +
                proteinLabel + " : " + decimalFormat.format(protein) + ", \n" +
                lipidsLabel + " : " + decimalFormat.format(lipids) + ", \n" +
                carbohydrateLabel + " : " + decimalFormat.format(carbohydrate) + ", \n" +
                dietaryFiberLabel + " : " + decimalFormat.format(dietaryFiber) + ", \n" +
                sodiumLabel + " : " + decimalFormat.format(sodium)
    }

    fun addFood(food: Food) {
        foodsList = foodsList.plus(food)
        calculateCalories(food, "add")
    }

    fun recalculateCalories() {
        calorieskcal = 0.0
        calorieskj = 0.0
        protein = 0.0
        carbohydrate = 0.0
        lipids = 0.0
        dietaryFiber = 0.0
        sodium = 0.0
        for (food in foodsList) {
            calculateCalories( food, "add")
        }
    }

    private fun calculateCalories(food: Food, operation: String) {
        val energyKcalFormatted = food.energyKcal.replace(Regex("(?i)[natr*]"), "0")
        val energyKjFormatted = food.energyKj.replace(Regex("(?i)[natr*]"), "0")
        val proteinFormatted = food.protein.replace(Regex("(?i)[natr*]"), "0")
        val carbohydrateFormatted = food.carbohydrate.replace(Regex("(?i)[natr*]"), "0")
        val lipidsFormatted = food.lipids.replace(Regex("(?i)[natr*]"), "0")
        val cholesterolFormatted = food.cholesterol.replace(Regex("(?i)[natr*]"), "0")
        val dietaryFiberFormatted = food.dietaryFiber.replace(Regex("(?i)[natr*]"), "0")
        val sodiumFormatted = food.sodium.replace(Regex("(?i)[natr*]"), "0")

        when (operation.lowercase()) {
            "add" -> {
                calorieskcal += ((energyKcalFormatted.toDouble() / 100) * food.grams)
                calorieskj += ((energyKjFormatted.toDouble() / 100) * food.grams)
                protein += ((proteinFormatted.toDouble() / 100) * food.grams)
                carbohydrate += ((carbohydrateFormatted.toDouble() / 100) * food.grams)
                lipids += ((lipidsFormatted.toDouble() / 100) * food.grams)
                cholesterol += ((cholesterolFormatted.toDouble() / 100) * food.grams)
                dietaryFiber += ((dietaryFiberFormatted.toDouble() / 100) * food.grams)
                sodium += ((sodiumFormatted.toDouble() / 100) * food.grams)
            }
            "subtract" -> {
                calorieskcal -= ((energyKcalFormatted.toDouble() / 100) * food.grams)
                calorieskj -= ((energyKjFormatted.toDouble() / 100) * food.grams)
                protein -= ((proteinFormatted.toDouble() / 100) * food.grams)
                carbohydrate -= ((carbohydrateFormatted.toDouble() / 100) * food.grams)
                lipids -= ((lipidsFormatted.toDouble() / 100) * food.grams)
                cholesterol -= ((cholesterolFormatted.toDouble() / 100) * food.grams)
                dietaryFiber -= ((dietaryFiberFormatted.toDouble() / 100) * food.grams)
                sodium -= ((sodiumFormatted.toDouble() / 100) * food.grams)
            }
        }
    }


}
