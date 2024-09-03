package com.example.anarcomarombismo.Controller

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyExercises(context: Context) {
    private val context = context
    private val jsonUtil = JSON()
    private val cache = Cache()
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatStored = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private fun getCurrentDate(): String = dateFormatStored.format(Date())

    fun exerciseDone(date: String = getCurrentDate(), exerciseID: Int,count: Int = 1) {
        val parsedDate = dateFormatInput.parse(date)
        val formattedDate = dateFormatStored.format(parsedDate)
        val exerciseList = getExerciseList(exerciseID).toMutableSet()

        val existingExercise = exerciseList.find { it.date == formattedDate && it.id == exerciseID }

        try {
            // Incrementa o valor de count se o exercício já existir na lista
            val updatedExercise = ExerciseByDate(existingExercise!!.date, existingExercise!!.id, existingExercise!!.count + 1)
            exerciseList.remove(existingExercise)
            exerciseList.add(updatedExercise)
        } catch (e: Exception) {
            // Adiciona um novo exercício com count 1 se não existir na lista
            exerciseList.add(ExerciseByDate(formattedDate, exerciseID, count))
        }
        updateCache(exerciseID, exerciseList.toList())
    }


    fun exerciseNotDone(date: String = getCurrentDate(), exerciseID: Int, days: Int = 1) {
        val startDate = dateFormatInput.parse(date) ?: return
        val exerciseList = getExerciseList(exerciseID).toMutableSet()
        val calendar = Calendar.getInstance().apply { time = startDate }

        repeat(days) {
            val checkDate = dateFormatStored.format(calendar.time)
            exerciseList.removeIf { it.date == checkDate && it.id == exerciseID }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        updateCache(exerciseID, exerciseList.toList())
    }

    fun getExercise(date: String = getCurrentDate(), exerciseID: Int, days: Int = 1): Boolean {
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

    fun getExerciseCount(exerciseID: Int): Int {
        val exerciseList = getExerciseList(exerciseID)
        if (exerciseList.isEmpty()) {
            return 0
        }
        val latestExercise = exerciseList.maxByOrNull { dateFormatStored.parse(it.date)?.time ?: 0 }
        return latestExercise?.count ?: 0
    }


    private fun getExerciseList(exerciseID: Int): Set<ExerciseByDate> {
        val cacheKey = "$exerciseID-exerciseList"
        return if (cache.hasCache(context, cacheKey)) {
            val json = cache.getCache(context, cacheKey)
            jsonUtil.fromJson(json, Array<ExerciseByDate>::class.java).toSet()
        } else {
            emptySet()
        }
    }

    private fun updateCache(exerciseID: Int, exerciseList: List<ExerciseByDate>) {
        val cacheKey = "$exerciseID-exerciseList"
        cache.setCache(context, cacheKey, jsonUtil.toJson(exerciseList))
    }
}
