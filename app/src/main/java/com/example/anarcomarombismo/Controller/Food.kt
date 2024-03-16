package com.example.anarcomarombismo.Controller

class Food (
    val foodNumber: String,
    val foodDescription: String,
    val moisture: String,
    val energyKcal: String,
    val energyKj: String,
    val protein: String,
    val lipids: String,
    val cholesterol: String,
    val carbohydrate: String,
    val dietaryFiber: String,
    val ash: String,
    val calcium: String,
    val magnesium: String,
    val manganese: String,
    val phosphorus: String,
    val iron: String,
    val sodium: String,
    val potassium: String,
    val copper: String,
    val zinc: String,
    val retinol: String,
    val re: String,
    val rae: String,
    val thiamine: String,
    val riboflavin: String,
    val pyridoxine: String,
    val niacin: String,
    val vitaminC: String
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
}