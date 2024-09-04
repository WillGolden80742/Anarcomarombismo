package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.formExercise
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAdapter(
    context: Context,
    private val exerciseList: Array<Exercise>,
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
        descriptionTextView.text = currentExercise.toString(context)
        val checkItem = listItemView.findViewById<FloatingActionButton>(R.id.checkBoxItem)
        val floatingEditExerciseActionButton = listItemView.findViewById<FloatingActionButton>(R.id.floatingEditExerciseActionButton)

        floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise, date)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
        }

        val checked = DailyExercises(context).getExercise(date, currentExercise.exerciseID, currentExercise.trainingID)

        if (checked) {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        } else {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        }

        countDays(labelCheckBoxItem, currentExercise)

        checkItem.setOnClickListener {
            handleExerciseCheck(currentExercise, labelCheckBoxItem, checkItem)
        }

        listItemView.setOnClickListener {
            callFormExercise("play", currentExercise, date)
            println("ID do exercício: ${currentExercise.exerciseID}")
        }

        return listItemView
    }

    private fun countDays(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        val countDays = dailyExercises.getExerciseDays(currentExercise.exerciseID, currentExercise.trainingID)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise.exerciseID, currentExercise.trainingID)
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
        val exerciseId = currentExercise.exerciseID
        val trainingId = currentExercise.trainingID
        val exerciseDone = dailyExercises.getExercise(date, exerciseId, trainingId)
        val exerciseDaysCount = dailyExercises.getExerciseDays(exerciseId, trainingId)
        val exerciseCount = dailyExercises.getExerciseCount(exerciseId, trainingId)
        val sets = currentExercise.sets

        if (shouldCheckExercise(exerciseDaysCount, exerciseCount, sets)) {
            markExerciseAsDone(dailyExercises, exerciseId, trainingId, checkItem)
        } else {
            toggleExerciseState(dailyExercises, exerciseDone, exerciseId, trainingId, checkItem)
        }

        updateDaysLabel(labelCheckBoxItem, currentExercise)
    }

    private fun shouldCheckExercise(exerciseDaysCount: Int, exerciseCount: Int, repetitions: Int): Boolean {
        return exerciseDaysCount == 0 && exerciseCount < repetitions
    }

    private fun markExerciseAsDone(dailyExercises: DailyExercises, exerciseId: Long, trainingId: Long, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        dailyExercises.exerciseDone(date, exerciseId, trainingId)
    }

    private fun toggleExerciseState(dailyExercises: DailyExercises, exerciseDone: Boolean, exerciseId: Long, trainingId: Long, checkItem: FloatingActionButton) {
        if (exerciseDone) {
            unmarkExerciseAsDone(dailyExercises, exerciseId, trainingId, checkItem)
        } else {
            markExerciseAsDone(dailyExercises, exerciseId, trainingId, checkItem)
            Toast.makeText(context, "${exerciseList.find { it.exerciseID == exerciseId }?.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unmarkExerciseAsDone(dailyExercises: DailyExercises, exerciseId: Long, trainingId: Long, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        dailyExercises.exerciseNotDone(date, exerciseId, trainingId)
    }

    private fun updateDaysLabel(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        countDays(labelCheckBoxItem, currentExercise)
    }
}
