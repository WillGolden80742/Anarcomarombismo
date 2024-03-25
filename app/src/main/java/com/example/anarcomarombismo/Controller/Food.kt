package com.example.anarcomarombismo.Controller

import java.text.DecimalFormat
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
    init {
        if (grams == null) {
            grams = 100.00
        }
    }
    override fun toString(): String {
        var text:String
        val decimalFormat = DecimalFormat("#.##")
        if (grams > 0.0) {
            text =
                """ Moisture (%): ${decimalFormat.format((moisture!!.toDouble() * grams) / 100)},
            | Energy (kcal): ${decimalFormat.format((energyKcal!!.toDouble() * grams) / 100)},
            | Energy (kJ): ${decimalFormat.format((energyKj!!.toDouble() * grams) / 100)},
            | Protein (g): ${decimalFormat.format((protein!!.toDouble() * grams) / 100)},
            | Lipids (g): ${decimalFormat.format((lipids!!.toDouble() * grams) / 100)},
            | Cholesterol (mg): ${decimalFormat.format((cholesterol!!.toDouble() * grams) / 100)},
            | Carbohydrate (g): ${decimalFormat.format((carbohydrate!!.toDouble() * grams) / 100)},
            | Dietary Fiber (g): ${decimalFormat.format((dietaryFiber!!.toDouble() * grams) / 100)},
            | Sodium (mg) : ${decimalFormat.format((sodium!!.toDouble() * grams) / 100)} """.trimMargin()
        } else {
            text =
                """ Moisture (%): ${decimalFormat.format(moisture!!.toDouble())},
            | Energy (kcal): ${decimalFormat.format(energyKcal!!.toDouble())},
            | Energy (kJ): ${decimalFormat.format(energyKj!!.toDouble())},
            | Protein (g): ${decimalFormat.format(protein!!.toDouble())},
            | Lipids (g): ${decimalFormat.format(lipids!!.toDouble())},
            | Cholesterol (mg): ${decimalFormat.format(cholesterol!!.toDouble())},
            | Carbohydrate (g): ${decimalFormat.format(carbohydrate!!.toDouble())},
            | Dietary Fiber (g): ${decimalFormat.format(dietaryFiber!!.toDouble())},
            | Sodium (mg) : ${decimalFormat.format(sodium!!.toDouble())} """.trimMargin()
        }
        return text
    }
}