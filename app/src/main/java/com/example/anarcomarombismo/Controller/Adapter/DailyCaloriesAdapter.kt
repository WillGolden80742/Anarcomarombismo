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
    private val listener: dailyCalories.OnItemClickListener,
    private val itemsPerPage: Int = 30
) : RecyclerView.Adapter<DailyCaloriesAdapter.ViewHolder>() {

    private val fullDailyCaloriesList: MutableList<DailyCalories> = mutableListOf()
    private val displayedDailyCaloriesList: MutableList<DailyCalories> = mutableListOf()
    private var isLoading = false
    private var currentPage = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.titleTextViewItem)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewItem)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(displayedDailyCaloriesList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCalories = displayedDailyCaloriesList[position]
        holder.dateTextView.text = currentCalories.date
        holder.descriptionTextView.text = currentCalories.toString(context)
    }

    override fun getItemCount() = displayedDailyCaloriesList.size

    // Load initial data and set up pagination
    fun loadData(dailyCaloriesList: List<DailyCalories>) {
        fullDailyCaloriesList.clear()
        fullDailyCaloriesList.addAll(dailyCaloriesList)

        // Reset pagination state
        currentPage = 0
        displayedDailyCaloriesList.clear()

        // Load first page
        loadNextPage()
    }

    // Load the next page of data
    fun loadNextPage() {
        // Prevent multiple simultaneous page loads
        if (isLoading) return
        isLoading = true

        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, fullDailyCaloriesList.size)

        if (startIndex < fullDailyCaloriesList.size) {
            val newItems = fullDailyCaloriesList.subList(startIndex, endIndex)

            // For first page, clear existing items
            if (currentPage == 0) {
                displayedDailyCaloriesList.clear()
            }

            // Add new items
            val oldSize = displayedDailyCaloriesList.size
            displayedDailyCaloriesList.addAll(newItems)

            // Notify adapter of changes
            if (currentPage == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeInserted(oldSize, newItems.size)
            }

            // Increment page and reset loading flag
            currentPage++
            isLoading = false
        }
    }

    // Check if more pages are available
    fun hasMorePages(): Boolean {
        return (currentPage * itemsPerPage) < fullDailyCaloriesList.size
    }

}