package com.example.anarcomarombismo.Controller.Adapter

import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.Forms.formExercise
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAdapter(
    context: Context,
    private val exerciseList: List<Exercise>,
    private var date: String = ""
) : ArrayAdapter<Exercise>(context, 0, exerciseList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.exercise_list_item, parent, false)
        }

        if (date.isEmpty()) {
            this.date = getCurrentDate()
        }

        val currentExercise = exerciseList[position]
        val labelCheckBoxItem = listItemView!!.findViewById<TextView>(R.id.labelCheckBoxItem)
        val nameTextView = listItemView.findViewById<TextView>(R.id.titleTextViewItem)
        nameTextView.text = currentExercise.name
        val descriptionTextView = listItemView.findViewById<TextView>(R.id.textViewItem)
        descriptionTextView.text = Html.fromHtml(currentExercise.toString(context), Html.FROM_HTML_MODE_COMPACT)
        val checkItem = listItemView.findViewById<FloatingActionButton>(R.id.checkBoxItem)
        val floatingEditExerciseActionButton = listItemView.findViewById<FloatingActionButton>(R.id.floatingEditExerciseActionButton)

        floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise, date)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
        }

        val checked = DailyExercises(context).isExerciseDone(date, currentExercise)

        if (checked) {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        } else {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        }

        countDays(labelCheckBoxItem, currentExercise)

        checkItem.setOnClickListener {
            handleExerciseCheck(currentExercise, labelCheckBoxItem, checkItem)
        }

        checkItem.setOnLongClickListener{
            val dailyExercises = DailyExercises(context)
            val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
            repeat(currentExercise.sets - exerciseCount) {
                handleExerciseCheck(currentExercise, labelCheckBoxItem, checkItem)
            }
            true
        }

        listItemView.setOnClickListener {
            callFormExercise("play", currentExercise, date)
            println("ID do exercício: ${currentExercise.exerciseID}")
        }

        return listItemView
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

    private fun callFormExercise(action: String, exercise: Exercise, date: String = getCurrentDate()) {
        val intent = Intent(context, formExercise::class.java)
        intent.putExtra("trainingID", exercise.trainingID)
        intent.putExtra("exerciseID", exercise.exerciseID)
        intent.putExtra("exerciseDate", date)
        intent.putExtra("action", action)
        println("ID do exercício: ${exercise.exerciseID}")
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
    }

    private fun toggleExerciseState(dailyExercises: DailyExercises, exerciseDone: Boolean, currentExercise: Exercise, checkItem: FloatingActionButton) {
        if (exerciseDone) {
            unmarkExerciseAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            markExerciseAsDone(dailyExercises, currentExercise, checkItem)
            Toast.makeText(context, "${exerciseList.find { it.exerciseID == currentExercise.exerciseID }?.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unmarkExerciseAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        dailyExercises.unmarkExercise(date, currentExercise)
    }

    private fun updateDaysLabel(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        countDays(labelCheckBoxItem, currentExercise)
    }
}
