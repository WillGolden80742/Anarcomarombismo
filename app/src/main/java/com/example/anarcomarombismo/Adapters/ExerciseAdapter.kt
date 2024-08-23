package com.example.anarcomarombismo.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
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
        // addPlayExerciseFormButton is CheckBox in simple_list_item.xml
        // <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent" android:layout_height="wrap_content" xmlns:app="http://schemas.android.com/apk/res-auto" android:padding="16dp"> <TextView android:id="@+id/titleTextViewItem" android:layout_width="match_parent" android:layout_height="wrap_content" android:textSize="24sp" android:textStyle="bold" android:paddingBottom="4dp" android:layout_alignParentStart="true" android:layout_alignParentTop="true" /> <TextView android:id="@+id/textViewItem" android:layout_width="match_parent" android:layout_height="wrap_content" android:textSize="18sp" android:paddingTop="4dp" android:layout_below="@id/titleTextViewItem" android:layout_alignParentStart="true" /> <LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="vertical" android:layout_alignParentEnd="true" android:layout_centerVertical="true" android:layout_marginStart="16dp"> <Switch android:id="@+id/switchItem" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginTop="20dp" /> <com.google.android.material.floatingactionbutton.FloatingActionButton android:id="@+id/floatingEditExerciseActionButton" android:layout_width="wrap_content" android:layout_height="wrap_content" android:layout_marginTop="16dp" android:clickable="true" app:backgroundTint="@color/button_color" app:srcCompat="@drawable/ic_fluent_text_bullet_list_square_edit_24_regular" app:tint="@color/yellow" /> </LinearLayout> </RelativeLayout>
        val switchItem  = listItemView!!.findViewById<Switch>(R.id.switchItem)
        val floatingEditExerciseActionButton = listItemView!!.findViewById<FloatingActionButton>(R.id.floatingEditExerciseActionButton)

        floatingEditExerciseActionButton.setOnClickListener {
            val intent = Intent(context, formExercise::class.java)
            intent.putExtra("trainingID", exerciseList[position].trainingID)
            intent.putExtra("exerciseID", exerciseList[position].exerciseID)
            println("ID do exercício: ${exerciseList[position].exerciseID}")
            context.startActivity(intent)
        }

        switchItem.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(context, "${exerciseList[position].name} "+ context.getString(R.string.finished), Toast.LENGTH_SHORT).show()
        }

        val currentExercise = exerciseList[position]

        val nameTextView = listItemView!!.findViewById<TextView>(R.id.titleTextViewItem)
        nameTextView.text = currentExercise.name

        val descriptionTextView = listItemView.findViewById<TextView>(R.id.textViewItem)
        descriptionTextView.text = currentExercise.toString(context)

        // Define um clique no item para iniciar a atividade addExercise
        listItemView.setOnClickListener {
            val intent = Intent(context, playExercise::class.java)
            // Passe os dados do exercício para a próxima atividade, se necessário
            intent.putExtra("trainingID", currentExercise.trainingID)
            intent.putExtra("exerciseID", currentExercise.exerciseID)
            println("ID do exercício: ${currentExercise.exerciseID}")
            context.startActivity(intent)
        }

        return listItemView
    }
}
