package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import com.example.anarcomarombismo.Controller.Exercise

class ContextualExercise(
    val trainingID: Long = 0,
    val linkVideo: String = "",
    val exerciseID: Long = 0,
    val name: Int = 0,
    val muscle: Int = 0,
    val sets: Int = 3,
    val repetitions: String = "10,10,10",
    val load: Double = 20.0,
    val rest: Int = 60,
    val cadence: String = "3-1-3"
) {
    companion object {
        fun getExercise(
            context: Context,
            exerciseRecoveryList: Array<ContextualExercise>
        ): List<Exercise> {
            val exerciseList = mutableListOf<Exercise>()
            for (exercise in exerciseRecoveryList) {
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