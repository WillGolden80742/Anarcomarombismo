package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.DailyCalories

class DailyCaloriesAdapter(context: Context, private val dailyCaloriesList: List<DailyCalories>) :
    ArrayAdapter<DailyCalories>(context, 0, dailyCaloriesList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        }

        val currentCalories = dailyCaloriesList[position]

        val dateTextView = listItemView!!.findViewById<TextView>(android.R.id.text1)
        dateTextView.text = currentCalories.date

        val descriptionTextView = listItemView.findViewById<TextView>(android.R.id.text2)
        descriptionTextView.text = currentCalories.toString()

        return listItemView
    }
}
