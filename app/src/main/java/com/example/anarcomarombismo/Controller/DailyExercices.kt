package com.example.anarcomarombismo.Controller

class DailyExercices (val excercise: Exercise, val date: String) {

    override fun toString() : String {
        return "Excercise " + excercise.name + " on " + date
    }

}