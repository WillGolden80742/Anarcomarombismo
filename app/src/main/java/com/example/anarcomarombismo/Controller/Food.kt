package com.example.anarcomarombismo.Controller

import java.text.DecimalFormat

class Food (
    var foodNumber: String = "0",
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