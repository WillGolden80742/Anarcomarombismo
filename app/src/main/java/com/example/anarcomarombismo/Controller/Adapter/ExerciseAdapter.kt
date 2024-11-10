package com.example.anarcomarombismo.Controller.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Util.WebHandler
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.Forms.formExercise
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAdapter(
    private val context: Context,
    private val exerciseList: List<Exercise>,
    private var date: String,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var isLoadingInterface = true

    companion object {

        private val itemPositionMap = mutableMapOf<Long, Int>()

        fun getItemPositionIndex(trainingId: Long): Int {
            return itemPositionMap[trainingId] ?: 0
        }

        fun setItemPositionIndex(trainingId: Long, index: Int) {
            itemPositionMap[trainingId] = index
        }

    }


    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webView: WebView = itemView.findViewById(R.id.webView)
        val labelCheckBoxItem: TextView = itemView.findViewById(R.id.labelCheckBoxItem)
        val nameTextView: TextView = itemView.findViewById(R.id.titleTextViewItem)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewItem)
        val checkItem: FloatingActionButton = itemView.findViewById(R.id.checkBoxItem)
        val floatingEditExerciseActionButton: FloatingActionButton = itemView.findViewById(R.id.floatingEditExerciseActionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.exercise_list_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]

        holder.nameTextView.text = currentExercise.name
        holder.descriptionTextView.text = currentExercise.toString(context)

        val webSettings: WebSettings = holder.webView.settings
        webSettings.javaScriptEnabled = true
        holder.webView.webViewClient = WebViewClient()
        WebHandler.embedVideo(context,holder.webView,WebHandler.generateYouTubeEmbedLink(currentExercise.linkVideo))
        holder.webView.setBackgroundColor(0x00000000)

        // Atualiza o estado do checkItem com base no exercício
        updateCheckItem(holder, currentExercise)

        // Contabiliza os dias desde o último exercício
        updateDaysLabel(holder.labelCheckBoxItem, currentExercise)

        holder.floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise)
        }

        holder.checkItem.setOnClickListener {
            handleExerciseCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
        }

        isLoadingInterface = true
        holder.checkItem.setOnLongClickListener {
            val dailyExercises = DailyExercises(context)
            var countDays = dailyExercises.getDaysSinceLastExercise(currentExercise)
            var exerciseCount = dailyExercises.getExerciseCount(currentExercise)
            var sets = currentExercise.sets
            if (exerciseCount == 0 || countDays > 0) {
                repeat(sets) {
                    handleExerciseCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
                }
            } else {
                unmarkExerciseAsDone(dailyExercises, currentExercise, holder.checkItem)
                countDays(holder.labelCheckBoxItem, currentExercise)
            }
            true
        }
        isLoadingInterface = false

        holder.itemView.setOnClickListener {
            callFormExercise("play", currentExercise)
        }
    }


    override fun getItemCount() = exerciseList.size

    private fun callFormExercise(action: String, exercise: Exercise) {
        val intent = Intent(context, formExercise::class.java).apply {
            putExtra("trainingID", exercise.trainingID)
            putExtra("exerciseID", exercise.exerciseID)
            putExtra("exerciseDate", date)
            putExtra("action", action)
        }
        context.startActivity(intent)
    }

    private fun getCurrentDate(): String {
        val currentDate = Date().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun handleExerciseCheck(currentExercise: Exercise, labelCheckBoxItem: TextView, checkItem: FloatingActionButton) {
        val dailyExercises = DailyExercises(context)
        val exerciseDone = dailyExercises.isExerciseDone(date, currentExercise)
        val exerciseDaysCount = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets

        if (shouldCheckExercise(exerciseDaysCount, exerciseCount, sets)) {
            markExerciseAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            toggleExerciseState(dailyExercises, exerciseDone, currentExercise, checkItem)
        }

        updateDaysLabel(labelCheckBoxItem, currentExercise)
    }


    private fun shouldCheckExercise(exerciseDaysCount: Int, exerciseCount: Int, repetitions: Int): Boolean {
        return exerciseDaysCount == 0 && exerciseCount < repetitions
    }

    private fun markExerciseAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        dailyExercises.markExerciseAsDone(date, currentExercise)
        val currentPosition = exerciseList.indexOf(currentExercise)
        val exerciseListSize = exerciseList.size
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        val isLastExercise= currentPosition == exerciseListSize - 1
        val isLastSet = exerciseCount == sets
        if (isLastExercise && isLastSet) {
            setItemPositionIndex(currentExercise.trainingID, 0)
        }
        if (isLastSet) {
            scrollToNextExercise(currentExercise)
        }
    }



    private fun toggleExerciseState(dailyExercises: DailyExercises, exerciseDone: Boolean, currentExercise: Exercise, checkItem: FloatingActionButton) {
        if (exerciseDone) {
            unmarkExerciseAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            markExerciseAsDone(dailyExercises, currentExercise, checkItem)
            Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unmarkExerciseAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        dailyExercises.unmarkExercise(date, currentExercise)
    }

    private fun updateDaysLabel(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        countDays(labelCheckBoxItem, currentExercise)
    }

    private fun countDays(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        val countDays = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        val daysText = when {
            countDays > 1 -> "$countDays ${context.getString(R.string.days)}"
            countDays == 1 -> "$countDays ${context.getString(R.string.day)}"
            countDays == 0 -> "$exerciseCount/$sets"
            else -> ""
        }
        labelCheckBoxItem.text = daysText
    }


    private fun scrollToNextExercise(currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        val exerciseListSize = exerciseList.size
        var nextPosition = (exerciseList.indexOf(currentExercise) + 1) % exerciseListSize

        while (true) {
            val nextExercise = exerciseList[nextPosition]
            val countDays = dailyExercises.getDaysSinceLastExercise(nextExercise)
            val exerciseCount = dailyExercises.getExerciseCount(nextExercise)
            val sets = nextExercise.sets
            val isExerciseDone = dailyExercises.isExerciseDone(date, nextExercise)

            // Se o próximo exercício ainda não foi completado, ou não foi realizado há algum tempo, rola para ele
            if (exerciseCount < sets || countDays > 0 || !isExerciseDone) {
                recyclerView.smoothScrollToPosition(nextPosition)
                break
            }

            // Move para o próximo exercício usando índice circular
            nextPosition = (nextPosition + 1) % exerciseListSize

            // Se voltamos ao exercício atual, significa que todos os exercícios foram completados
            if (nextPosition == exerciseList.indexOf(currentExercise)) break
        }
    }


    private fun updateCheckItem(holder: ExerciseViewHolder, currentExercise: Exercise) {
        val checked = DailyExercises(context).isExerciseDone(date, currentExercise)
        holder.checkItem.setImageResource(
            if (checked) R.drawable.ic_fluent_select_all_on_24_filled
            else R.drawable.ic_fluent_select_all_off_24_regular
        )
    }
}
