package com.example.anarcomarombismo.Controller.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCalories

class DailyCaloriesAdapter(
    private val context: Context,
    private val dailyCaloriesList: List<DailyCalories>,
    private val listener: dailyCalories.OnItemClickListener // Adicione o listener aqui
) : RecyclerView.Adapter<DailyCaloriesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.titleTextViewItem)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewItem)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(dailyCaloriesList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCalories = dailyCaloriesList[position]
        holder.dateTextView.text = currentCalories.date
        holder.descriptionTextView.text = currentCalories.toString(context)
    }

    override fun getItemCount() = dailyCaloriesList.size
}
