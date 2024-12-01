package com.example.anarcomarombismo.Controller

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Macro (
    var calories: Double = 1960.0,
    var lipids: Double = 42.0,
    var lipidsByWeight: Double = 0.6,
    var carbs: Double = 255.5,
    var protein: Double = 140.0,
    var proteinByWeight: Double = 2.0,
    var dietaryFiber: Double = 20.0,
) : DataHandler<Macro> {

    companion object {
        private var cache = Cache();
        private const val cacheKey = "Macro"
        fun build(calories: Double, lipids: Double, lipidsByWeight: Double, carbs: Double, protein: Double, proteinByWeight: Double, dietaryFiber: Double): Macro {
            return Macro(
                calories = calories,
                lipids = lipids,
                lipidsByWeight = lipidsByWeight,
                carbs = carbs,
                protein = protein,
                proteinByWeight = proteinByWeight,
                dietaryFiber = dietaryFiber
            )
        }

    }
    override fun save(context: Context): Boolean {
        cache.setCache(context,cacheKey,listOf(this))
        return true

    }

    override fun remove(context: Context): Boolean {
        cache.setCache(context,cacheKey,listOf(null))
        return true
    }

    override fun fetchById(context: Context, id: Any): Macro? {
        return if (cache.hasCache(context,cacheKey)) {
            fetchAll(context).first()
        } else {
            Macro()
        }
    }

    fun fetch(context: Context): Macro? {
        return fetchById(context,0)
    }
    override fun fetchAll(context: Context): List<Macro> {
        return if (cache.hasCache(context,cacheKey)) {
            cache.getCache(context, cacheKey, Array<Macro>::class.java).toList()
        } else {
            listOf(Macro())
        }
    }

    fun loadAndUpdateMacroUI(
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
        miniVersion: Boolean = false,
        macro: Macro = fetch(context)?: Macro()
    ) {
        val dailyCalories = macroNutrients(context)
        updateProgressBars(dailyCalories, macro, caloriesProgressBar, carbsProgressBar, fatsProgressBar, proteinsProgressBar,dietaryFiberProgressBar)
        updateLabels(dailyCalories, macro, context, caloriesLabel, carbsLabel, fatsLabel, proteinsLabel,dietaryFiberLabel,miniVersion)
    }

    fun loadAndUpdateMacroUI(
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
        miniVersion: Boolean = false,
        dailyCalories: Map<String, Double>
    ) {
        val macro: Macro = fetch(context)?: Macro()
        updateProgressBars(dailyCalories, macro, caloriesProgressBar, carbsProgressBar, fatsProgressBar, proteinsProgressBar,dietaryFiberProgressBar)
        updateLabels(dailyCalories, macro, context, caloriesLabel, carbsLabel, fatsLabel, proteinsLabel,dietaryFiberLabel,miniVersion)
    }

    private fun macroNutrients(context: Context,days:Int=7): Map<String, Double> {
        val dailyCaloriesList = DailyCalories().fetchLast7Days(context)
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
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_purple) else progressBar.context.getDrawable(
                    R.drawable.progress_bar_purple)
            }
            R.id.carbsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_white) else progressBar.context.getDrawable(
                    R.drawable.progress_bar_white)
            }
            R.id.fatsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_yellow) else progressBar.context.getDrawable(
                    R.drawable.progress_bar_yellow)
            }
            R.id.proteinsProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_red) else progressBar.context.getDrawable(
                    R.drawable.progress_bar_red)
            }
            R.id.dietaryFiberProgressBar -> {
                if (exceeded) progressBar.context.getDrawable(R.drawable.progress_exceed_bar_green) else progressBar.context.getDrawable(
                    R.drawable.progress_bar_green)
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



}