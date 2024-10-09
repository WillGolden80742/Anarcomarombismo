package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.R

class ContextualExercise {
    companion object {
        fun getExercises(
            context: Context
        ): Map<Long, Array<Exercise>> {
            return mapOf(
                1L to arrayOf(
                    Exercise(1, "https://www.youtube.com/watch?v=Pw-zpREJ7xo", 1, context.getString(R.string.dumbbell_press),context.getString(R.string.breastplate),4,"10,10,10,10", 100.0),
                    Exercise(1, "https://www.youtube.com/watch?v=Ky_JXqloq0w", 2, context.getString(
                        R.string.dumbbell_press_30),context.getString(R.string.breastplate),4,"10,10,10,10", 90.0),
                    Exercise(1, "https://youtu.be/0hsnZxrhAY8?si=ZF0VDqFCBmpCkYwZ&t=331", 3,context.getString(
                        R.string.dumbbell_press_less30),context.getString(R.string.breastplate),4,"10,10,10,10", 20.0),
                    Exercise(1, "https://www.youtube.com/watch?v=q_Qs9fdwscs", 4, context.getString(
                        R.string.pullover_with_halter_on_the_black_bench),context.getString(R.string.breastplate),4,"10,10,10,10", 50.0),
                    Exercise(1, "https://www.youtube.com/watch?v=qLtVeISKSeA&t=123s", 5, context.getString(
                        R.string.skull_crushers), context.getString(R.string.triceps),4,"10,10,10,10", 30.0),
                    Exercise(1, "https://www.youtube.com/watch?v=cQ5ae1dHTAQ", 6, context.getString(
                        R.string.triceps_pulley), context.getString(R.string.triceps),4,"10,10,10,10", 0.0),
                    Exercise(1, "https://www.youtube.com/watch?v=XYjcAZFNPnc", 7, context.getString(
                        R.string.triceps_french_on_low_poly), context.getString(R.string.triceps),4,"10,10,10,10", 0.0),
                    Exercise(1, "https://www.youtube.com/watch?v=iZEN4DK5BFM", 8, context.getString(
                        R.string.triceps_bilateral_machine), context.getString(R.string.triceps),4,"10,10,10,10", 0.0)
                ),
                2L to arrayOf(
                    Exercise(2, "https://www.youtube.com/watch?v=vUu_4jBxM1c", 1, context.getString(
                        R.string.front_triangle_pulley), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=YywSCu4Y360", 2, context.getString(
                        R.string.pulley_front_bar), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=gH_nPs_DoQI", 3, context.getString(
                        R.string.articulated_pulley), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=mUgFn3aMAP4", 4, context.getString(
                        R.string.low_stroke), context.getString(R.string.back),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=FHyZEuRpSg4", 5, context.getString(
                        R.string.biceps_bar_w),  context.getString(R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=ITRfzXEcBz0", 6, context.getString(
                        R.string.hammer_thread_alternate_each_side_counts_as_repeated),context.getString(
                        R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://www.youtube.com/watch?v=1lR_dT07wBM", 7, context.getString(
                        R.string.straight_thread_on_rope),context.getString(R.string.biceps),4,"10,10,10,10", 0.0),
                    Exercise(2, "https://youtu.be/QtGkO8fRI6c?si=Vh2VoMPC3-0tsjPg&t=366", 8, context.getString(
                        R.string.thread_spider),context.getString(R.string.biceps),4,"10,10,10,10", 0.0)
                ),
                3L to arrayOf(
                    Exercise(3, "https://www.youtube.com/watch?v=eufDL9MmF8A", 1, context.getString(
                        R.string.development_with_halter),context.getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=c7zMmbWkUPw", 2, context.getString(
                        R.string.lateral_elevation),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=kKjjeiXL960", 3, context.getString(
                        R.string.frontal_elevation),context.getString(R.string.anterior_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/IwWvZ0rlNXs?si=e9hu1OEBA0ikhvpL&t=45", 4, context.getString(
                        R.string.sitting_lateral_elevation),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=XIJdRoAHHj4", 5, context.getString(
                        R.string.high_paddle_with_bar_w), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 6, context.getString(
                        R.string.shrinkage_with_halter_on_the_side_of_the_body), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 7, context.getString(
                        R.string.shrinkage_with_halter_in_front_of_body), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://youtu.be/RhGjwIUe16E?si=wSfdW_EbDDs2DbZW", 8, context.getString(
                        R.string.shrinking_with_body_halter), context.getString(R.string.trapezium),4,"10,10,10,10", 0.0),
                    Exercise(3, "https://www.youtube.com/watch?v=IwWvZ0rlNXs", 9, context.getString(
                        R.string.side_lift_on_inclined_bench),context.getString(R.string.lateral_deltoids),4,"10,10,10,10", 0.0)
                ),
                4L to arrayOf(
                    Exercise(4, "https://www.youtube.com/watch?v=emujvqD_Pq8", 1, context.getString(
                        R.string.calves), context.getString(R.string.calves),6,"15,15,15,15,15,15", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=g-73WZ_c6m4", 2, context.getString(
                        R.string.free_squat), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=adPY6cd4h58", 3, context.getString(
                        R.string.leg_press_45), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=pTUfuTLoTQU", 4, context.getString(
                        R.string.flex_chair), context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=I_uBK4DDflU", 5, context.getString(
                        R.string.extension_chair), context.getString(R.string.quadriceps),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=KIoiwCfcTXM", 6, context.getString(
                        R.string.flex_table), context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 7, context.getString(
                        R.string.machine_leg_abduction), context.getString(R.string.glutes),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=SNu9SM_j3b4", 8, context.getString(
                        R.string.adduction_leg_on_machine), context.getString(R.string.adductors),4,"10,10,10,10", 0.0),
                    Exercise(4, "https://www.youtube.com/watch?v=_6ElJLyBXcE", 9, context.getString(
                        R.string.squat_stiff),context.getString(R.string.thigh_back),4,"10,10,10,10", 0.0)
                )
            )
        }
    }

}