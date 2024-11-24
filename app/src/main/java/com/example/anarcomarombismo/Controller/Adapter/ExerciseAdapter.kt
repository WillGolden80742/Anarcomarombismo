package com.example.anarcomarombismo.Controller.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_UP
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
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.WebHandler
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.Forms.formExercise
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAdapter(
    private val context: Context,
    private val exerciseList: List<Exercise>,
    private var date: String,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    companion object {

        private val cache = Cache()
        fun getItemPositionIndex(context: Context,trainingId: Long): Int {
            val positionKey = "exercise_position_$trainingId"
            if (cache.hasCache(context,positionKey)) {
                return cache.getCache(context,positionKey,Int::class.java)
            } else {
                cache.setCache(context,positionKey,0)
                return 0
            }
        }

        fun setItemPositionIndex(context: Context,trainingId: Long, index: Int) {
            val positionKey = "exercise_position_$trainingId"
            cache.setCache(context,positionKey,index)
        }

    }

    interface OnExerciseCheckListener {
        fun onExerciseCheckChanged()
    }

    private val checkListener = context as? OnExerciseCheckListener

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]

        holder.nameTextView.text = currentExercise.name
        holder.descriptionTextView.text = currentExercise.toString(context)

        val webSettings: WebSettings = holder.webView.settings
        webSettings.javaScriptEnabled = true
        holder.webView.webViewClient = WebViewClient()
        CoroutineScope(Dispatchers.Main).launch {
            val embedLink = WebHandler.generateYouTubeEmbedLink(currentExercise.linkVideo)
            WebHandler.embedVideo(context, holder.webView, embedLink)
        }
        holder.webView.setBackgroundColor(0x00000000)

        // Atualiza o estado do checkItem com base no exercício
        updateCheckItem(holder, currentExercise)

        // Contabiliza os dias desde o último exercício
        updateDaysLabel(holder.labelCheckBoxItem, currentExercise)

        holder.floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise)
        }

        holder.checkItem.setOnClickListener {
            handleSetsCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
            checkListener?.onExerciseCheckChanged()
            val dailyExercises = DailyExercises(context)
            val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
            val sets = currentExercise.sets
            if (exerciseCount == sets) {
                Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
            }
        }

        var isLongPressActive = false
        holder.checkItem.setOnLongClickListener {
            isLongPressActive = true
            Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
            true
        }

        holder.checkItem.setOnTouchListener { _, event ->
            when (event.action) {
                ACTION_UP -> {
                    if (isLongPressActive) {
                        checkSets(holder, currentExercise)
                        isLongPressActive = false
                    }
                }
                ACTION_CANCEL -> {
                    isLongPressActive = false
                }
            }
            false
        }

        holder.itemView.setOnClickListener {
            callFormExercise("play", currentExercise)
        }
    }

    private fun checkSets(holder: ExerciseViewHolder, currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        var countDays = dailyExercises.getDaysSinceLastExercise(currentExercise)
        var exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        var sets = currentExercise.sets
        if (exerciseCount == 0 || countDays > 0) {
            repeat(sets) {
                handleSetsCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
            }
        } else {
            unmarkExerciseAsDone(dailyExercises, currentExercise, holder.checkItem)
            countDays(holder.labelCheckBoxItem, currentExercise)
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

    private fun handleSetsCheck(currentExercise: Exercise, labelCheckBoxItem: TextView, checkItem: FloatingActionButton) {
        val dailyExercises = DailyExercises(context)
        val exerciseDone = dailyExercises.isExerciseDone(date, currentExercise)
        val exerciseDaysCount = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets

        if (shouldCheckExercise(exerciseDaysCount, exerciseCount, sets)) {
            markSetsAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            toggleExerciseState(dailyExercises, exerciseDone, currentExercise, checkItem)
        }
        updateDaysLabel(labelCheckBoxItem, currentExercise)
    }


    private fun shouldCheckExercise(exerciseDaysCount: Int, setsCount: Int, repetitions: Int): Boolean {
        return exerciseDaysCount == 0 && setsCount < repetitions
    }

    private fun markSetsAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        dailyExercises.markSetsAsDone(date, currentExercise)
        val currentPosition = exerciseList.indexOf(currentExercise)
        val exerciseListSize = exerciseList.size
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        val isLastExercise= currentPosition == exerciseListSize - 1
        val isLastSet = exerciseCount == sets
        if (isLastExercise && isLastSet) {
            setItemPositionIndex(context,currentExercise.trainingID, 0)
        }
        if (isLastSet) {
            scrollToNextExercise(currentExercise)
        }
    }



    private fun toggleExerciseState(dailyExercises: DailyExercises, exerciseDone: Boolean, currentExercise: Exercise, checkItem: FloatingActionButton) {
        if (exerciseDone) {
            unmarkExerciseAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            markSetsAsDone(dailyExercises, currentExercise, checkItem)
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
