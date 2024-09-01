package com.example.anarcomarombismo.Controller

import android.content.Context
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DailyExercises(context: Context) {
    private val context = context
    private val jsonUtil = JSON()
    private val cache = Cache()
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatStored = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private fun getCurrentDate(): String = dateFormatStored.format(Date())

    fun exerciseDone(date: String = getCurrentDate(), exerciseID: Int) {
        val parsedDate = dateFormatInput.parse(date)
        val formattedDate = dateFormatStored.format(parsedDate)
        val exerciseByDate = ExerciseByDate(formattedDate, exerciseID)
        val exerciseList = getExerciseList(exerciseID).toMutableList().apply {
            add(exerciseByDate)
        }
        updateCache(exerciseID, exerciseList)
    }

    fun exerciseNotDone(date: String = getCurrentDate(), exerciseID: Int, days: Int = 7) {
        val startDate = dateFormatInput.parse(date) ?: return
        val exerciseList = getExerciseList(exerciseID).toMutableList()
        val calendar = Calendar.getInstance().apply { time = startDate }

        repeat(days) {
            val checkDate = dateFormatStored.format(calendar.time)
            exerciseList.removeIf { it.date == checkDate && it.id == exerciseID }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        updateCache(exerciseID, exerciseList)
    }

    fun getExercise(date: String = getCurrentDate(), exerciseID: Int, days: Int = 7): Boolean {
        val startDate = dateFormatInput.parse(date) ?: return false
        val calendar = Calendar.getInstance().apply { time = startDate }

        repeat(days) {
            val checkDate = dateFormatStored.format(calendar.time)
            if (getExerciseList(exerciseID).any { it.date == checkDate && it.id == exerciseID }) {
                return true
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return false
    }

    fun getExerciseDays(exerciseID: Int): Int {
        val exerciseList = getExerciseList(exerciseID)
        if (exerciseList.isEmpty()) {
            return -1 // Retorna 0 ou qualquer outro valor que denote que não há registros
        }
        val latestExerciseDate = exerciseList.maxOfOrNull {
            dateFormatStored.parse(it.date)
        } ?: return -1
        val currentDate = dateFormatStored.parse(getCurrentDate())
        val diffInMillis = currentDate?.time?.minus(latestExerciseDate.time) ?: 0
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        return diffInDays
    }


    private fun getExerciseList(exerciseID: Int): List<ExerciseByDate> {
        val cacheKey = "$exerciseID-exerciseList"
        return if (cache.hasCache(context, cacheKey)) {
            val json = cache.getCache(context, cacheKey)
            jsonUtil.fromJson(json, Array<ExerciseByDate>::class.java).toList()
        } else {
            emptyList()
        }
    }

    private fun updateCache(exerciseID: Int, exerciseList: List<ExerciseByDate>) {
        val cacheKey = "$exerciseID-exerciseList"
        cache.setCache(context, cacheKey, jsonUtil.toJson(exerciseList))
    }
}