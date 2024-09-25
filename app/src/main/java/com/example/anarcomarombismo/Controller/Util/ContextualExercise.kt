package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import com.example.anarcomarombismo.Controller.Exercise

class ContextualExercise(
    private val trainingID: Long = 0,
    private val linkVideo: String = "",
    private val exerciseID: Long = 0,
    private val name: Int = 0,
    private val muscle: Int = 0,
    private val sets: Int = 3,
    private val repetitions: String = "10,10,10",
    private val load: Double = 20.0,
    private val rest: Int = 60,
    private val cadence: String = "3-1-3"
) {
    companion object {
        fun getExercise(
            context: Context,
            contextualExerciseList: Array<ContextualExercise>
        ): List<Exercise> {
            val exerciseList = mutableListOf<Exercise>()
            for (exercise in contextualExerciseList) {
                exerciseList.add(
                    exercise.toExercise(context)
                )
            }
            return exerciseList
        }
    }
    private fun toExercise(context: Context): Exercise {
        return Exercise(
            trainingID = trainingID,
            linkVideo = linkVideo,
            exerciseID = exerciseID,
            name = context.getString(name),
            muscle = context.getString(muscle),
            sets = sets,
            repetitions = repetitions,
            load = load,
            rest = rest,
            cadence = cadence
        )
    }

}