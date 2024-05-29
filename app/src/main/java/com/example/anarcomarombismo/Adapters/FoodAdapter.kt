package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Food
import com.example.anarcomarombismo.R

class FoodAdapter(context: Context, foodList: List<Food>) :
    ArrayAdapter<Food>(context, 0, foodList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
        }

        val currentItem = getItem(position)

        val descriptionTextView = listItemView!!.findViewById<TextView>(R.id.titleTextViewItem)
        val detailsTextView = listItemView.findViewById<TextView>(R.id.textViewItem)

        descriptionTextView.text = currentItem?.foodDescription
        detailsTextView.text = currentItem?.toString()

        return listItemView
    }
}
