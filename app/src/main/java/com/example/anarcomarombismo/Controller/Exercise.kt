package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.R

class Exercise(
    var trainingID: Long,
    var LinkVideo: String ,
    var exerciseID: Long,
    var name: String = "Exercício",
    var muscle: String = "",
    var sets: Int = 3,
    var repetitions: String = "10,10,10",
    var load: Double = 20.0, // Carga padrão em kg
    var rest: Int = 60, // Tempo de repouso padrão em segundos
    var cadence: String = "3-1-3" // Cadência padrão
) {

    fun toString(context: Context): String {
        return "${context.getString(R.string.muscle)}: $muscle, \n${context.getString(R.string.sets)}: $sets,\n${context.getString(R.string.reps)}: $repetitions, \n${context.getString(R.string.load)}: $load, \n${context.getString(R.string.rest)}: $rest, \n${context.getString(R.string.cadence)}: $cadence"
    }

    override fun toString(): String {
        return "Muscle: $muscle, \nsets: $sets, \nrepetitions: $repetitions, \nload:$load, \nrest: $rest, \ncadence: '$cadence'"
    }
}
