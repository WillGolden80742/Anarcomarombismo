package com.example.anarcomarombismo.Controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.Controller.Util.ShareFiles
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCalories
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
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

        fun export(context: Context): Boolean {
            val contextualKey = context.getString(R.string.dailycalories)
            val dailyCaloriesList = cache.getCache(context, contextualKey, Array<DailyCalories>::class.java)

            if (dailyCaloriesList.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.no_daily_calories_to_export), Toast.LENGTH_SHORT).show()
                return false
            }

            val jsonDailyCalories = JSON.toJson(dailyCaloriesList)

            return ShareFiles.exportToFile(
                context = context,
                fileName = "DailyCalories.anarchy3",
                content = jsonDailyCalories,
                onSuccess = {
                    // No additional action needed as Toast is handled in ShareFiles
                },
                onError = {
                    Toast.makeText(context, context.getString(R.string.no_daily_calories_to_export), Toast.LENGTH_SHORT).show()
                }
            )
        }

        fun import(context: Context, uri: Uri) {
            ShareFiles.importFromFile(
                context = context,
                uri = uri,
                onSuccess = { content ->
                    try {
                        val importedDailyCalories = JSON.fromJson(content, Array<DailyCalories>::class.java)
                        saveDailyCaloriesList(context, importedDailyCalories.toList())
                        Toast.makeText(context, context.getString(R.string.daily_calories_imported_successfully), Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, dailyCalories::class.java)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("DailyCalories", "Error parsing file", e)
                        Toast.makeText(context, context.getString(R.string.error_reading_file), Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    Toast.makeText(context, context.getString(R.string.error_reading_file), Toast.LENGTH_SHORT).show()
                }
            )
        }

        private fun saveDailyCaloriesList(context: Context, dailyCaloriesList: List<DailyCalories>) {
            val contextualKey = context.getString(R.string.dailycalories)
            cache.setCache(context, contextualKey, dailyCaloriesList)
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
                    .append(decimalFormat.format(calorieskcal)).append("kcal").append(", \n")
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
