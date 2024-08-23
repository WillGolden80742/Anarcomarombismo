package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.formExercise
import com.example.anarcomarombismo.playExercise
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExerciseAdapter(context: Context, private val exerciseList: Array<Exercise>) : ArrayAdapter<Exercise>(context, 0, exerciseList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
        }

        val floatingExerciseActionButton = listItemView!!.findViewById<FloatingActionButton>(R.id.floatingExerciseActionButton)
        val floatingEditExerciseActionButton = listItemView.findViewById<FloatingActionButton>(R.id.floatingEditExerciseActionButton)

        floatingEditExerciseActionButton.setOnClickListener {
            val intent = Intent(context, formExercise::class.java)
            intent.putExtra("trainingID", exerciseList[position].trainingID)
            intent.putExtra("exerciseID", exerciseList[position].exerciseID)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
            context.startActivity(intent)
        }

        floatingExerciseActionButton.setOnClickListener {
            val intent = Intent(context, playExercise::class.java)
            intent.putExtra("trainingID", exerciseList[position].trainingID)
            intent.putExtra("exerciseID", exerciseList[position].exerciseID)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
            context.startActivity(intent)
        }

        val currentExercise = exerciseList[position]

        val nameTextView = listItemView!!.findViewById<TextView>(R.id.titleTextViewItem)
        nameTextView.text = currentExercise.name

        val descriptionTextView = listItemView.findViewById<TextView>(R.id.textViewItem)
        descriptionTextView.text = currentExercise.toString(context)

        // Define um clique no item para iniciar a atividade addExercise
        listItemView.setOnClickListener {
            val intent = Intent(context, formExercise::class.java)
            // Passe os dados do exercício para a próxima atividade, se necessário
            intent.putExtra("trainingID", currentExercise.trainingID)
            intent.putExtra("exerciseID", currentExercise.exerciseID)
            println("ID do exercício: ${currentExercise.exerciseID}")
            context.startActivity(intent)
        }

        return listItemView
    }
}
