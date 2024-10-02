package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyCalories(
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
        if (date.isEmpty()) {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            date = dateFormat.format(currentDate)
        }
    }

    companion object {
        private val cache = Cache()

        fun build(
            date: String = "",
            foodsList: List<Food> = listOf()
        ): DailyCalories {
            return DailyCalories(date = date, foodsList = foodsList).apply {
                recalculateCalories()
            }
        }
    }

    override fun save(context: Context): Boolean {
        val contextualKey = context.getString(R.string.dailycalories)
        return try {
            val dailyCaloriesList = getExistingDailyCaloriesList(context)
            val updatedCaloriesList = dailyCaloriesList.filterNot { it.date == this.date } + this
            cache.setCache(context, contextualKey, updatedCaloriesList)
            true
        } catch (e: Exception) {
            println("Error saving daily calories: $e")
            false
        }
    }

    override fun remove(context: Context): Boolean {
        val contextualKey = context.getString(R.string.dailycalories)
        val existingList = cache.getCache(context, contextualKey, Array<DailyCalories>::class.java).toList()

        existingList?.let {
            val updatedCaloriesList = removeCaloriesForDate(it, date)
            if (updatedCaloriesList.size !=  it.size) {
                cache.setCache(context, contextualKey, updatedCaloriesList)
                Toast.makeText(context, context.getString(R.string.daily_calories_removed_successfully), Toast.LENGTH_SHORT).show()
                resetDailyCalories(date)
            }
        }
        return true
    }

    fun loadMacroTarget(
        context: Context,
        caloriesProgressBar: ProgressBar,
        carbsProgressBar: ProgressBar,
        fatsProgressBar: ProgressBar,
        proteinsProgressBar: ProgressBar,
        dietaryFiberProgressBar: ProgressBar,
        caloriesLabel: TextView,
        carbsLabel: TextView,
        fatsLabel: TextView,
        proteinsLabel: TextView,
        dietaryFiberLabel: TextView,
        miniVersion: Boolean = false
    ) {
        Macro().fetch(context)?.let { macroTarget ->
            val dailyCalories = macroNutrients(context)
            updateProgressBars(dailyCalories, macroTarget, caloriesProgressBar, carbsProgressBar, fatsProgressBar, proteinsProgressBar,dietaryFiberProgressBar)
            updateLabels(dailyCalories, macroTarget, context, caloriesLabel, carbsLabel, fatsLabel, proteinsLabel,dietaryFiberLabel,miniVersion)
        }
    }

    private fun macroNutrients(context: Context,days:Int=7): Map<String, Double> {
        val dailyCaloriesList = fetchAll(context)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(days+1)) // Get date 7 days ago
        val sevenDaysAgo = calendar.time

        val last7DaysCalories = dailyCaloriesList.filter { dailyCalories ->
            val dailyCaloriesDate = dateFormat.parse(dailyCalories.date)
            dailyCaloriesDate != null && !dailyCaloriesDate.before(sevenDaysAgo)
        }

        val size = last7DaysCalories.size
        if (size == 0) {
            return mapOf(
                "Calories" to 0.0,
                "Protein" to 0.0,
                "Carbohydrates" to 0.0,
                "Lipids" to 0.0,
                "DietaryFiber" to 0.0
            )
        }

        return mapOf(
            "Calories" to last7DaysCalories.sumOf { it.calorieskcal } / size,
            "Protein" to last7DaysCalories.sumOf { it.protein } / size,
            "Carbohydrates" to last7DaysCalories.sumOf { it.carbohydrate } / size,
            "Lipids" to last7DaysCalories.sumOf { it.lipids } / size,
            "DietaryFiber" to last7DaysCalories.sumOf { it.dietaryFiber } / size
        )
    }
    private fun updateProgressBars(
        dailyCalories: Map<String, Double>,
        macro: Macro,
        caloriesProgressBar: ProgressBar,
        carbsProgressBar: ProgressBar,
        fatsProgressBar: ProgressBar,
        proteinsProgressBar: ProgressBar,
        dietaryFiberProgressBar: ProgressBar,
    ) {
        val calories = dailyCalories["Calories"] ?: 0.0
        val carbs = dailyCalories["Carbohydrates"] ?: 0.0
        val lipids = dailyCalories["Lipids"] ?: 0.0
        val protein = dailyCalories["Protein"] ?: 0.0
        val dietaryFiber = dailyCalories["DietaryFiber"] ?: 0.0
        caloriesProgressBar.progress = calculateProgress(caloriesProgressBar, calories, macro.calories)
        carbsProgressBar.progress = calculateProgress(carbsProgressBar, carbs, macro.carbs)
        fatsProgressBar.progress = calculateProgress(fatsProgressBar, lipids, macro.lipids)
        proteinsProgressBar.progress = calculateProgress(proteinsProgressBar, protein, macro.protein)
        dietaryFiberProgressBar.progress = calculateProgress(dietaryFiberProgressBar, dietaryFiber, macro.dietaryFiber)
    }

    private fun calculateProgress(progressBar: ProgressBar, currentValue: Double, targetValue: Double): Int {
        val progress: Int
        val exceeded = currentValue > targetValue
        progressBar.progressDrawable = when (progressBar.id) {
            R.id.caloriesProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_purple) else progressBar.context.getDrawable(R.drawable.progress_bar_purple)
            }
            R.id.carbsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_white) else progressBar.context.getDrawable(R.drawable.progress_bar_white)
            }
            R.id.fatsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_yellow) else progressBar.context.getDrawable(R.drawable.progress_bar_yellow)
            }
            R.id.proteinsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_red) else progressBar.context.getDrawable(R.drawable.progress_bar_red)
            }
            R.id.dietaryFiberProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_green) else progressBar.context.getDrawable(R.drawable.progress_bar_green)
            }
            else -> progressBar.progressDrawable
        }
        progress = if (exceeded) {
            (((currentValue % targetValue).toInt()).toDouble() / targetValue * 100).toInt()
        } else {
            ((currentValue / targetValue) * 100).toInt()
        }
        return progress
    }

    private fun updateLabels(
        dailyCalories: Map<String, Double>,
        macro: Macro,
        context: Context,
        caloriesLabel: TextView,
        carbsLabel: TextView,
        fatsLabel: TextView,
        proteinsLabel: TextView,
        dietaryFiberLabel: TextView,
        miniVersion: Boolean
    ) {
        val decimalFormat = DecimalFormat("#.#")
        val dia = context.getString(R.string.day)

        // Create a list of labels and their corresponding targets
        val nutrients = listOf(
            Pair("Calories", caloriesLabel) to macro.calories,
            Pair("Carbohydrates", carbsLabel) to macro.carbs,
            Pair("Lipids", fatsLabel) to macro.lipids,
            Pair("Protein", proteinsLabel) to macro.protein,
            Pair("DietaryFiber", dietaryFiberLabel) to macro.dietaryFiber
        )

        nutrients.forEach { (nutrientLabel, target) ->
            val nutrientValue = dailyCalories[nutrientLabel.first] ?: 0.0
            val percentage = (nutrientValue / target * 100).toInt()

            if (!miniVersion) {
                nutrientLabel.second.text =
                    "${context.getString(getLabelResource(nutrientLabel.first))} ${decimalFormat.format(nutrientValue)}${if (nutrientLabel.first == "Calories") "kcal" else "g"}/$dia ($percentage%)"
            } else {
                nutrientLabel.second.text =
                    "${context.getString(getLabelResource(nutrientLabel.first))} $percentage%"
            }
        }
    }

    // Helper function to get label resource ID
    private fun getLabelResource(nutrient: String): Int {
        return when (nutrient) {
            "Calories" -> R.string.calories_b
            "Carbohydrates" -> R.string.carbohydrates_b
            "Lipids" -> R.string.lipids_b
            "Protein" -> R.string.proteins_b
            "DietaryFiber" -> R.string.dietary_fiber_b
            else -> throw IllegalArgumentException("Unknown nutrient: $nutrient")
        }
    }



    override fun fetchById(context: Context, id: Any): DailyCalories {
        val contextualKey = context.getString(R.string.dailycalories)
        return if (cache.hasCache(context, contextualKey)) {
            val dailyCaloriesList = cache.getCache(context, contextualKey, Array<DailyCalories>::class.java).toList()
            dailyCaloriesList.find { it.date == id as String } ?: DailyCalories().apply { date = id as String }
        } else {
            DailyCalories().apply { date = id as String }
        }
    }



    override fun fetchAll(context: Context): List<DailyCalories> {
        val contextualKey = context.getString(R.string.dailycalories)
        val dailyCaloriesList = if (cache.hasCache(context, contextualKey)) {
            cache.getCache(context, contextualKey, Array<DailyCalories>::class.java).toList()
        } else {
            emptyList()
        }
        return dailyCaloriesList.sortedByDescending { it.date.split("/").let { dateParts -> "${dateParts[2]}${dateParts[1]}${dateParts[0]}".toInt() } }
    }

    private fun getExistingDailyCaloriesList(context: Context): List<DailyCalories> {
        val contextualKey = context.getString(R.string.dailycalories)
        return if (cache.hasCache(context, contextualKey)) {
            cache.getCache(context, contextualKey, Array<DailyCalories>::class.java).toList()
        } else {
            emptyList()
        }
    }
    private fun removeCaloriesForDate(
        dailyCaloriesList: List<DailyCalories>,
        date: String
    ): List<DailyCalories> {
        return dailyCaloriesList.filterNot { it.date == date }
    }

    private fun resetDailyCalories(date: String) {
        this.date = date
        this.foodsList = emptyList() // Reset food list if needed
    }

    fun addFood(food: Food) {
        foodsList = foodsList + food
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
            calculateCalories(food, "add")
        }
    }

    private fun calculateCalories(food: Food, operation: String) {
        val calorieMultiplier = (food.grams / 100.0)

        when (operation.lowercase()) {
            "add" -> {
                calorieskcal += (food.energyKcal.toDouble() * calorieMultiplier)
                calorieskj += (food.energyKj.toDouble() * calorieMultiplier)
                protein += (food.protein.toDouble() * calorieMultiplier)
                carbohydrate += (food.carbohydrate.toDouble() * calorieMultiplier)
                lipids += (food.lipids.toDouble() * calorieMultiplier)
                cholesterol += (food.cholesterol.toDouble() * calorieMultiplier)
                dietaryFiber += (food.dietaryFiber.toDouble() * calorieMultiplier)
                sodium += (food.sodium.toDouble() * calorieMultiplier)
            }
            "subtract" -> {
                calorieskcal -= (food.energyKcal.toDouble() * calorieMultiplier)
                calorieskj -= (food.energyKj.toDouble() * calorieMultiplier)
                protein -= (food.protein.toDouble() * calorieMultiplier)
                carbohydrate -= (food.carbohydrate.toDouble() * calorieMultiplier)
                lipids -= (food.lipids.toDouble() * calorieMultiplier)
                cholesterol -= (food.cholesterol.toDouble() * calorieMultiplier)
                dietaryFiber -= (food.dietaryFiber.toDouble() * calorieMultiplier)
                sodium -= (food.sodium.toDouble() * calorieMultiplier)
            }
        }
    }

    fun toString(context: Context): String {
        if (foodsList.isEmpty()) {
            return context.getString(R.string.no_calories_added_yet)
        } else {
            val decimalFormat = DecimalFormat("#")
            return buildString {
                append(context.getString(R.string.calories_b)).append(" : ")
                    .append(decimalFormat.format(calorieskcal)).append("kcal").append(" - ")
                    .append(decimalFormat.format(calorieskj)).append("kj").append(", \n")
                append(context.getString(R.string.proteins_b)).append(" : ")
                    .append(decimalFormat.format(protein)).append(context.getString(R.string.g)).append(", \n")
                append(context.getString(R.string.lipids_b)).append(" : ")
                    .append(decimalFormat.format(lipids)).append(context.getString(R.string.g)).append(", \n")
                append(context.getString(R.string.carbohydrates_b)).append(" : ")
                    .append(decimalFormat.format(carbohydrate)).append(context.getString(R.string.g)).append(", \n")
                append(context.getString(R.string.dietary_fiber_b)).append(" : ")
                    .append(decimalFormat.format(dietaryFiber)).append(context.getString(R.string.g)).append(", \n")
                append(context.getString(R.string.sodium_b)).append(" : ")
                    .append(decimalFormat.format(sodium)).append(context.getString(R.string.mg))
            }
        }
    }
}
