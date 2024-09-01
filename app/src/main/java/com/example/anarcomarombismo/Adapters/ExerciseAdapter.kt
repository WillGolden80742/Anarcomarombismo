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

class ExerciseAdapter(context: Context, private val exerciseList: Array<Exercise>, private var date:String="") : ArrayAdapter<Exercise>(context, 0, exerciseList) {
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
        val nameTextView = listItemView!!.findViewById<TextView>(R.id.titleTextViewItem)
        nameTextView.text = currentExercise.name
        val descriptionTextView = listItemView.findViewById<TextView>(R.id.textViewItem)
        descriptionTextView.text = currentExercise.toString(context)
        val checkItem  = listItemView!!.findViewById<FloatingActionButton>(R.id.checkBoxItem)
        val floatingEditExerciseActionButton = listItemView!!.findViewById<FloatingActionButton>(R.id.floatingEditExerciseActionButton)


        floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise,date)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
        }

        val checked = DailyExercises(context).getExercise(date,currentExercise.exerciseID.toInt())

        if (checked) {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        } else {
            checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        }

        countDays(labelCheckBoxItem,currentExercise)

        val dailyExercices = DailyExercises(context)
        checkItem.setOnClickListener {
            val exerciceDone = dailyExercices.getExercise(date,currentExercise.exerciseID.toInt())
            if (exerciceDone) {
                checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
                dailyExercices.exerciseNotDone(date,currentExercise.exerciseID.toInt())
                countDays(labelCheckBoxItem,currentExercise)
            } else {
                checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
                dailyExercices.exerciseDone(date,currentExercise.exerciseID.toInt())
                Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
            }
        }

        listItemView.setOnClickListener {
            callFormExercise("play", currentExercise,date)
            println("ID do exercício: ${currentExercise.exerciseID}")
        }

        return listItemView
    }

    private fun countDays (labelCheckBoxItem: TextView, currentExercise: Exercise) {
        val countDays = DailyExercises(context).getExerciseDays(currentExercise.exerciseID.toInt())

        if (countDays >= 0) {
            labelCheckBoxItem.text = "$countDays "+ context.getString(R.string.dias)
            if (countDays == 1) {
                labelCheckBoxItem.text = labelCheckBoxItem.text.substring(0, labelCheckBoxItem.text.length - 1)
            }
        } else {
            labelCheckBoxItem.text = ""
        }
    }
    private fun callFormExercise(action: String, exercise: Exercise, date:String=getCurrentDate()) {
        val intent = Intent(context, formExercise::class.java)
        intent.putExtra("trainingID", exercise.trainingID)
        intent.putExtra("exerciseID", exercise.exerciseID)
        intent.putExtra("exerciseDate", date)
        intent.putExtra("action", action)
        println("ID do exercício: ${exercise.exerciseID}")
        context.startActivity(intent)
    }

    fun getCurrentDate(): String {
        val currentDate = Date().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        return formattedDate
    }
}
