package com.example.anarcomarombismo.Adapters

import JSON
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.exercises

class TrainingAdapter(context: Context, private val trainingList: Array<Training>, private val listener: OnTrainingItemClickListener) : ArrayAdapter<Training>(context, 0, trainingList) {

    interface OnTrainingItemClickListener {
        fun onItemClick(training: Training)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
        }

        val currentTraining = trainingList[position]

        val nameTextView = listItemView!!.findViewById<TextView>(android.R.id.text1)
        nameTextView.text = currentTraining.name

        val descriptionTextView = listItemView.findViewById<TextView>(android.R.id.text2)
        descriptionTextView.text = currentTraining.description

        listItemView.setOnClickListener {
            listener.onItemClick(currentTraining)
            var intent = Intent(context, exercises::class.java)
            intent.putExtra("trainingID", currentTraining.trainingID)
            context.startActivity(intent)
        }

        return listItemView
    }
}
