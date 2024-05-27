package com.example.anarcomarombismo.Controller

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

    override fun toString(): String {
        val decimalFormat = DecimalFormat("#.##")
        val formattedProtein = decimalFormat.format(protein)
        val formattedCarbohydrate = decimalFormat.format(carbohydrate)
        val formattedLipids = decimalFormat.format(lipids)
        val formattedCholesterol = decimalFormat.format(cholesterol)
        val formattedDietaryFiber = decimalFormat.format(dietaryFiber)
        val formattedSodium = decimalFormat.format(sodium)
        val formattedCaloriesKcal = decimalFormat.format(calorieskcal)
        val formattedCaloriesKj = decimalFormat.format(calorieskj)
        return "$formattedCaloriesKcal kcal, $formattedCaloriesKj kj\n$formattedProtein g protein, $formattedCarbohydrate g carbohydrate, $formattedLipids g lipids, $formattedCholesterol mg cholesterol, $formattedDietaryFiber g dietary fiber, $formattedSodium mg sodium"
    }

    fun addFood(food: Food) {
        foodsList = foodsList.plus(food)
        calculateCalories(food, "add")
    }

    fun removeFood(food: Food) {
        calculateCalories(food, "subtract")
        foodsList = foodsList.minus(food)
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
