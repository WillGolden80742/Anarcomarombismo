package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCaloriesFoods
import com.example.anarcomarombismo.formDailyCalories
import com.example.anarcomarombismo.formFoods
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodAdapter(context: Context, foodList: List<Food>, activity:String="formDailyCalories") : ArrayAdapter<Food>(context, 0, foodList) {

    private val activity = activity

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.food_list_item, parent, false)
        }

        val currentItem = getItem(position)

        val descriptionTextView = listItemView!!.findViewById<TextView>(R.id.foodTitleTextViewItem)
        val detailsTextView = listItemView.findViewById<TextView>(R.id.foodTextViewItem)
        val editButton = listItemView.findViewById<FloatingActionButton>(R.id.foodFloatingEditActionButton)
        val addButton = listItemView.findViewById<FloatingActionButton>(R.id.foodFloatingAddActionButton)

        descriptionTextView.text = currentItem?.foodDescription
        detailsTextView.text = currentItem?.toString(context)

        when (activity) {
            "formDailyCalories" -> {
                editButton.setOnClickListener {
                    val intent = Intent(context, formFoods::class.java).apply {
                        putExtra("foodID", currentItem?.foodNumber)
                    }
                    context.startActivity(intent)
                }

                addButton.setOnClickListener {
                    try {
                        Toast.makeText(
                            context,
                            "\"${currentItem?.foodDescription}\""+ context.getString(R.string.selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        var formDailyCalories = context as formDailyCalories
                        currentItem?.let { it1 -> formDailyCalories.selectedFood(it1) }
                    } catch (e: Exception) {
                        Toast.makeText(context,
                            context.getString(R.string.add_food_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "dailyCaloriesFoods" -> {
                addButton.isVisible = false
                editButton.setImageResource(R.drawable.ic_fluent_delete_24_regular)
                editButton.setOnClickListener {
                    try {
                        Toast.makeText(
                            context,
                            "${currentItem?.foodDescription} removed",
                            Toast.LENGTH_SHORT
                        ).show()
                        var dailyCaloriesFoods = context as dailyCaloriesFoods
                        currentItem?.let { it1 -> dailyCaloriesFoods.removeFood(it1) }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao remover food", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "loading" -> {
                descriptionTextView.text = context.getString(R.string.loading)
                detailsTextView.text = context.getString(R.string.please_wait)
                editButton.isVisible = false
                addButton.isVisible = false
            }
        }

        return listItemView
    }


}
