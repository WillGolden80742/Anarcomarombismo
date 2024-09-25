package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyCalories (
    var date: String = "",
    var protein: Double = 0.0,
    var carbohydrate: Double = 0.0,
    var lipids: Double = 0.0,
    var cholesterol: Double = 0.0,
    var dietaryFiber: Double = 0.0,
    var sodium: Double = 0.0,
    var calorieskcal: Double = 0.0,
    var calorieskj: Double = 0.0,
    var foodsList: List<Food> = listOf()
) : DataHandler<DailyCalories> {

    init {
        if (date == "") {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            date = formattedDate
        }
    }

    companion object {
        private val cache = Cache()
        private val json = JSON()
        fun build(
            date: String = "",
            foodsList: List<Food> = listOf()
        ): DailyCalories {
            val dailyCalories = DailyCalories().apply {
                this.date = date
                this.foodsList = foodsList
            }
            dailyCalories.recalculateCalories()
            return dailyCalories
        }
    }

    override fun save(context: Context): Boolean {
        val contextualKey = context.getString(R.string.dailycalories)
        return try {
            val dailyCaloriesList = getExistingDailyCaloriesList(context)
            val updatedCaloriesList = dailyCaloriesList.filterNot { it.date == this.date } + this
            cache.setCache(context, contextualKey, json.toJson(updatedCaloriesList))
            true
        } catch (e: Exception) {
            handleError(e, "Error saving daily calories")
            false
        }
    }

    override fun remove(context: Context):Boolean {
        val currentDate = date
        val contextualKey = context.getString(R.string.dailycalories)
        val cache = Cache()

        cache.getCache(context, contextualKey)?.let { dailyCaloriesListJson ->
            val dailyCaloriesList = parseDailyCaloriesList(dailyCaloriesListJson)
            val updatedCaloriesList = removeCaloriesForDate(dailyCaloriesList, currentDate)
            if (updatedCaloriesList.size != dailyCaloriesList.size) {
                cache.setCache(context, contextualKey, toJson(updatedCaloriesList))
                showToast(context, R.string.daily_calories_removed_successfully)
                resetDailyCalories(currentDate)
            }
        }
        return true
    }

    override fun fetchById(context: Context, id: Any): DailyCalories {
        val contextualKey = context.getString(R.string.dailycalories)
        if (cache.hasCache(context, contextualKey)) {
            val dailyCaloriesListJson = cache.getCache(context, contextualKey)
            val json = JSON()
            val dailyCaloriesList = json.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
            val dailyCaloriesListFiltered = dailyCaloriesList.filter { it.date == id as String }
            return if (dailyCaloriesListFiltered.isNotEmpty()) {
                dailyCaloriesListFiltered[0]
            } else {
                DailyCalories().apply { date = id as String}
            }
        } else {
            return DailyCalories().apply { date = id as String}
        }
    }

    override fun fetchAll(context: Context): List<DailyCalories> {
        val contextualKey = context.getString(R.string.dailycalories)
        val emptyContextualKey = context.getString(R.string.emptydailycalories)
        var dailyCaloriesList: List<DailyCalories> = emptyList()
        try {
            if (cache.hasCache(context, contextualKey)) {
                val dailyCaloriesListJson = cache.getCache(context, contextualKey)
                println("Lista de calorias diárias: $dailyCaloriesListJson")
                dailyCaloriesList = json.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
            } else {
                val dailyCaloriesListJson = cache.getCache(context, emptyContextualKey)
                println("Lista de calorias diárias: $dailyCaloriesListJson")
                dailyCaloriesList = json.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
            }
            dailyCaloriesList = dailyCaloriesList.sortedByDescending { dailyCalories ->
                val dateParts = dailyCalories.date.split("/")
                "${dateParts[2]}${dateParts[1]}${dateParts[0]}".toInt()
            }
        } catch (e: Exception) {
            println("Erro ao carregar a lista de calorias diárias: $e")
        }
        return dailyCaloriesList.toList()
    }

    private fun getExistingDailyCaloriesList(context: Context): List<DailyCalories> {
        val contextualKey = context.getString(R.string.dailycalories)
        return if (cache.hasCache(context, contextualKey)) {
            val dailyCaloriesListJson = cache.getCache(context, contextualKey)
            json.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
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
        val energyKcalFormatted = formatNutrientValue(food.energyKcal)
        val energyKjFormatted = formatNutrientValue(food.energyKj)
        val proteinFormatted = formatNutrientValue(food.protein)
        val carbohydrateFormatted = formatNutrientValue(food.carbohydrate)
        val lipidsFormatted = formatNutrientValue(food.lipids)
        val cholesterolFormatted = formatNutrientValue(food.cholesterol)
        val dietaryFiberFormatted = formatNutrientValue(food.dietaryFiber)
        val sodiumFormatted = formatNutrientValue(food.sodium)

        val calorieMultiplier = (food.grams / 100.0)

        when (operation.lowercase()) {
            "add" -> {
                calorieskcal += (energyKcalFormatted.toDouble() * calorieMultiplier)
                calorieskj += (energyKjFormatted.toDouble() * calorieMultiplier)
                protein += (proteinFormatted.toDouble() * calorieMultiplier)
                carbohydrate += (carbohydrateFormatted.toDouble() * calorieMultiplier)
                lipids += (lipidsFormatted.toDouble() * calorieMultiplier)
                cholesterol += (cholesterolFormatted.toDouble() * calorieMultiplier)
                dietaryFiber += (dietaryFiberFormatted.toDouble() * calorieMultiplier)
                sodium += (sodiumFormatted.toDouble() * calorieMultiplier)
            }
            "subtract" -> {
                calorieskcal -= (energyKcalFormatted.toDouble() * calorieMultiplier)
                calorieskj -= (energyKjFormatted.toDouble() * calorieMultiplier)
                protein -= (proteinFormatted.toDouble() * calorieMultiplier)
                carbohydrate -= (carbohydrateFormatted.toDouble() * calorieMultiplier)
                lipids -= (lipidsFormatted.toDouble() * calorieMultiplier)
                cholesterol -= (cholesterolFormatted.toDouble() * calorieMultiplier)
                dietaryFiber -= (dietaryFiberFormatted.toDouble() * calorieMultiplier)
                sodium -= (sodiumFormatted.toDouble() * calorieMultiplier)
            }
        }
    }
    private fun formatNutrientValue(nutrientValue: String): String {
        return nutrientValue.replace(Regex("(?i)[natr*]"), "0")
    }
}
