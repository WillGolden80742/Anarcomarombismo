package com.example.anarcomarombismo.Controller.Adapter

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
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCaloriesFoods
import com.example.anarcomarombismo.Forms.formDailyCalories
import com.example.anarcomarombismo.Forms.formFood
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FoodAdapter(
    context: Context,
    foodList: List<Food>,
    private val activity: String = "formDailyCalories"
) : ArrayAdapter<Food>(context, 0, foodList) {

    private val inflater = LayoutInflater.from(context)
    private val doubleClickTimeDelta: Long = 300
    private var lastClickTime: Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: inflater.inflate(R.layout.food_list_item, parent, false)
        val currentItem = getItem(position) ?: return listItemView
        bindViews(listItemView, currentItem)
        return listItemView
    }

    private fun bindViews(view: View, food: Food) {
        val descriptionTextView = view.findViewById<TextView>(R.id.foodTitleTextViewItem)
        val detailsTextView = view.findViewById<TextView>(R.id.foodTextViewItem)
        val editButton = view.findViewById<FloatingActionButton>(R.id.foodFloatingEditActionButton)
        val addButton = view.findViewById<FloatingActionButton>(R.id.foodFloatingAddActionButton)

        descriptionTextView.text = food.foodDescription
        detailsTextView.text = food.toString(context)

        handleActivityState(activity, food, descriptionTextView, detailsTextView, editButton, addButton)
    }

    private fun handleActivityState(
        activity: String,
        food: Food,
        descriptionTextView: TextView,
        detailsTextView: TextView,
        editButton: FloatingActionButton,
        addButton: FloatingActionButton
    ) {
        when (activity) {
            "formDailyCalories" -> formDailyCalories(food, editButton, addButton)
            "dailyCaloriesFoods" -> dailyCaloriesFoods(food, editButton, addButton)
            "loading" -> loading(descriptionTextView, detailsTextView, editButton, addButton)
        }
    }

    private fun loading(
        descriptionTextView: TextView,
        detailsTextView: TextView,
        editButton: FloatingActionButton,
        addButton: FloatingActionButton
    ) {
        descriptionTextView.text = context.getString(R.string.loading)
        detailsTextView.text = context.getString(R.string.please_wait)
        editButton.isVisible = false
        addButton.isVisible = false
    }

    private fun formDailyCalories(food: Food, editButton: FloatingActionButton, addButton: FloatingActionButton) {
        addButton.setOnClickListener {
            addFoodToDailyCalories(food)
        }
        editButton.setOnClickListener {
            navigateToFormFoods(food)
        }
    }

    private fun navigateToFormFoods(food: Food) {
        val intent = Intent(context, formFood::class.java).apply {
            putExtra("foodID", food.foodNumber)
            putExtra("foodObject", JSON.toJson(food))
        }
        context.startActivity(intent)
    }

    private fun addFoodToDailyCalories(food: Food) {
        try {
            Toast.makeText(
                context,
                "\"${food.foodDescription}\" " + context.getString(R.string.selected),
                Toast.LENGTH_SHORT
            ).show()

            val dailyCalories = context as? formDailyCalories
            dailyCalories?.selectedFood(food)
        } catch (e: Exception) {
            showToast(R.string.add_food_error)
        }
    }

    private fun dailyCaloriesFoods(food: Food, editButton: FloatingActionButton, addButton: FloatingActionButton) {
        addButton.isVisible = false
        editButton.setImageResource(R.drawable.ic_fluent_delete_24_regular)
        editButton.setOnClickListener {
            handleFoodRemoval(food)
        }
    }

    private fun handleFoodRemoval(food: Food) {
        val clickTime = System.currentTimeMillis()
        if (isDoubleClick(clickTime)) {
            removeFood(food)
        } else {
            showToast(R.string.double_click_fast_for_exclusion)
        }
        lastClickTime = clickTime
    }

    private fun isDoubleClick(clickTime: Long): Boolean {
        return clickTime - lastClickTime < doubleClickTimeDelta
    }

    private fun removeFood(food: Food) {
        try {
            val removed = context.getString(R.string.removed)
            Toast.makeText(context, "${food.foodDescription} $removed", Toast.LENGTH_SHORT).show()
            val dailyCalories = context as? dailyCaloriesFoods
            dailyCalories?.removeFood(food)
        } catch (e: Exception) {
            showToast(R.string.food_removal_error)
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}
