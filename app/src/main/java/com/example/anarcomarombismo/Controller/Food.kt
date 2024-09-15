package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.R
import java.text.DecimalFormat
import java.util.UUID

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

    fun toString(context: Context): String {
        val decimalFormat = DecimalFormat("#.##")
        val gramsLabel = context.getString(R.string.grams)
        val energyKcalLabel = context.getString(R.string.energy_kcal)
        val energyKjLabel = context.getString(R.string.energy_kj)
        val proteinLabel = context.getString(R.string.protein)
        val lipidsLabel = context.getString(R.string.lipids)
        val carbohydrateLabel = context.getString(R.string.carbohydrate)
        val dietaryFiberLabel = context.getString(R.string.dietary_fiber)
        val sodiumLabel = context.getString(R.string.sodium)

        return if (grams > 0.0) {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
            energyKcalLabel + " : " + decimalFormat.format((energyKcal!!.toDouble() * grams) / 100.0) + ",\n" +
            energyKjLabel + " : " + decimalFormat.format((energyKj!!.toDouble() * grams) / 100.0) + ",\n" +
            proteinLabel + " : " + decimalFormat.format((protein!!.toDouble() * grams) / 100.0) + ",\n" +
            lipidsLabel + " : " + decimalFormat.format((lipids!!.toDouble() * grams) / 100.0) + ",\n" +
            carbohydrateLabel + " : " + decimalFormat.format((carbohydrate!!.toDouble() * grams) / 100.0) + ",\n" +
            dietaryFiberLabel + " : " + decimalFormat.format((dietaryFiber!!.toDouble() * grams) / 100.0) + ",\n" +
            sodiumLabel + " : " + decimalFormat.format((sodium!!.toDouble() * grams) / 100.0)
        } else {
            gramsLabel + " : " + decimalFormat.format(grams) + ",\n" +
            energyKcalLabel + " : " + decimalFormat.format(energyKcal!!.toDouble()) + ",\n" +
            energyKjLabel + " : " + decimalFormat.format(energyKj!!.toDouble()) + ",\n" +
            proteinLabel + " : " + decimalFormat.format(protein!!.toDouble()) + ",\n" +
            lipidsLabel + " : " + decimalFormat.format(lipids!!.toDouble()) + ",\n" +
            carbohydrateLabel + " : " + decimalFormat.format(carbohydrate!!.toDouble()) + ",\n" +
            dietaryFiberLabel + " : " + decimalFormat.format(dietaryFiber!!.toDouble()) + ",\n" +
            sodiumLabel + " : " + decimalFormat.format(sodium!!.toDouble())
        }
    }
}