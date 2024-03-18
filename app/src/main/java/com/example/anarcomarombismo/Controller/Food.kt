package com.example.anarcomarombismo.Controller

import kotlin.reflect.KMutableProperty1

class Food (
    var foodNumber: String,
    var grams: Double = 100.0,
    var foodDescription: String,
    var moisture: String,
    var energyKcal: String,
    var energyKj: String,
    var protein: String,
    var lipids: String,
    var cholesterol: String,
    var carbohydrate: String,
    var dietaryFiber: String,
    var ash: String,
    var calcium: String,
    var magnesium: String,
    var manganese: String,
    var phosphorus: String,
    var iron: String,
    var sodium: String,
    var potassium: String,
    var copper: String,
    var zinc: String,
    var retinol: String,
    var re: String,
    var rae: String,
    var thiamine: String,
    var riboflavin: String,
    var pyridoxine: String,
    var niacin: String,
    var vitaminC: String
) {
    override fun toString(): String {
        return """
            Nutritional Information:
            ---------------------------------------
            Food Number: $foodNumber\n
            Food Description: $foodDescription\n
            Moisture (%): $moisture\n
            Energy (kcal): $energyKcal\n
            Energy (kJ): $energyKj\n
            Protein (g): $protein\n
            Lipids (g): $lipids\n
            Cholesterol (mg): $cholesterol\n
            Carbohydrate (g): $carbohydrate\n
            Dietary Fiber (g): $dietaryFiber\n
            Ash (g): $ash\n
            Calcium (mg): $calcium\n
            Magnesium (mg): $magnesium\n
            Manganese (mg): $manganese\n
            Phosphorus (mg): $phosphorus\n
            Iron (mg): $iron\n
            Sodium (mg): $sodium\n
            Potassium (mg): $potassium\n
            Copper (mg): $copper\n
            Zinc (mg): $zinc\n
            Retinol (mcg): $retinol\n
            RE (mcg): $re\n
            RAE (mcg): $rae\n
            Thiamine (mg): $thiamine\n
            Riboflavin (mg): $riboflavin\n
            Pyridoxine (mg): $pyridoxine\n
            Niacin (mg): $niacin\n
            Vitamin C (mg): $vitaminC\n
            ---------------------------------------
        """.trimIndent()
    }

    fun toStringInLine(): String {
        return "$foodNumber - $foodDescription - $energyKcal kcal - $protein g - $carbohydrate g - $lipids g - $dietaryFiber g - $sodium mg"
    }
}