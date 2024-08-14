package com.example.anarcomarombismo.Controller

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyExercices () {
    private lateinit var date: String
    private lateinit var exercise: Exercise

    fun addExercise(exercise: Exercise) {
        this.exercise = exercise
    }

    init {
        if (date == "") {
            val currentDate = Date().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            date = formattedDate
        }
    }

    override fun toString() : String {
        return "Excercise " + exercise.name + " on " + date
    }

}