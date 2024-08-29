package com.example.anarcomarombismo.Controller

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyExercices(context: Context) {

    private val context = context
    private val jsonUtil = JSON()
    private val cache = Cache()

    fun getCurrentDate(): String {
        val currentDate = Date().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        return formattedDate
    }
    fun exerciceDone(date: String = getCurrentDate(), exerciceID: Int) {
        val exercicesList = getExerciceList(date).toMutableList()
        exercicesList.add(exerciceID.toString())
        cache.setCache(context, "$date-exercicesList", jsonUtil.toJson(exercicesList))
    }

    fun exerciceNotDone(date: String = getCurrentDate(), exerciceID:Int) {
        val exercicesList = getExerciceList(date).toMutableList()
        exercicesList.remove(exerciceID.toString())
        cache.setCache(context, "$date-exercicesList", jsonUtil.toJson(exercicesList))
    }
    fun getExercice(date: String = getCurrentDate(), exerciceID: Int): Boolean {
        return getExerciceList(date).contains(exerciceID.toString())
    }

    private fun getExerciceList(date: String = getCurrentDate()): List<String> {
        if (cache.hasCache(context, "$date-exercicesList")) {
            val json = cache.getCache(context, "$date-exercicesList")
            return jsonUtil.fromJson(json, Array<String>::class.java).toList()
        } else {
            return listOf()
        }
    }
}
