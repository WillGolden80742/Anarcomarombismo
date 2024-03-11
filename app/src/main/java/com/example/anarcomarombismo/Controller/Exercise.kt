package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.R

class Exercise(
    var trainingID: Long,
    var exerciseID: Long,
    var name: String = "Exercício",
    var sets: Int = 3,
    var repetitions: String = "10,10,10",
    var load: Double = 20.0, // Carga padrão em kg
    var rest: Int = 60, // Tempo de repouso padrão em segundos
    var cadence: String = "3-1-3" // Cadência padrão
) {

    fun toString(context: Context): String {
        return "${context.getString(R.string.sets)}: $sets, ${context.getString(R.string.reps)}: $repetitions, ${context.getString(R.string.load)}: $load, ${context.getString(R.string.rest)}: $rest, ${context.getString(R.string.cadence)}: $cadence"
    }

    override fun toString(): String {
        return "sets: $sets, repetitions: $repetitions, load:$load, rest: $rest, cadence: '$cadence'"
    }
}
