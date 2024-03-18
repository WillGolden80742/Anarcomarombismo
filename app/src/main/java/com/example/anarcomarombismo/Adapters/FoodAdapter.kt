package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Food

class FoodAdapter(context: Context, foodList: List<Food>) :
    ArrayAdapter<Food>(context, 0, foodList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewVar = convertView
        if (convertViewVar == null) {
            convertViewVar = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        }

        val currentItem = getItem(position)

        val text1 = convertViewVar!!.findViewById<TextView>(android.R.id.text1)
        val text2 = convertViewVar.findViewById<TextView>(android.R.id.text2)

        text1.text = currentItem?.foodDescription
        text2.text = """ Moisture (%): ${currentItem?.moisture} - Energy (kcal): ${currentItem?.energyKcal} - Energy (kJ): ${currentItem?.energyKj} - Protein (g): ${currentItem?.protein} - Lipids (g): ${currentItem?.lipids} - Cholesterol (mg): ${currentItem?.cholesterol} - Carbohydrate (g): ${currentItem?.carbohydrate} - Dietary Fiber (g): ${currentItem?.dietaryFiber} - Sodium (mg) : ${currentItem?.sodium} """.trimIndent()

        return convertViewVar
    }
}
