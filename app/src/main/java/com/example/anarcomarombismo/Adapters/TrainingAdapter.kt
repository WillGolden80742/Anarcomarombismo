package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Training
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.exercises

class TrainingAdapter(context: Context, private val trainingList: List<Training>, private val listener: OnTrainingItemClickListener) : ArrayAdapter<Training>(context, 0, trainingList) {

    interface OnTrainingItemClickListener {
        fun onItemClick(training: Training)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.training_list_item, parent, false)
        }

        val currentTraining = trainingList[position]

        val titleTextView = listItemView!!.findViewById<TextView>(R.id.trainingTitleTextViewItem)
        titleTextView.text = currentTraining.name

        val descriptionTextView = listItemView.findViewById<TextView>(R.id.trainingTextViewItem)
        descriptionTextView.text = currentTraining.description

        listItemView.setOnClickListener {
            listener.onItemClick(currentTraining)
            val intent = Intent(context, exercises::class.java)
            intent.putExtra("trainingID", currentTraining.trainingID)
            context.startActivity(intent)
        }

        return listItemView
    }
}
