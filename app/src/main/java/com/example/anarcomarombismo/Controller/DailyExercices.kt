package com.example.anarcomarombismo.Controller

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Classe responsável por gerenciar exercícios diários
class DailyExercices(context: Context) {
    // Referência ao contexto da aplicação
    private val context = context

    // Instâncias de utilitários para manipulação de JSON e cache
    private val jsonUtil = JSON()
    private val cache = Cache()

    // Formatos de data para entrada e armazenamento
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatStored = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    // Função para obter a data atual no formato de armazenamento
    fun getCurrentDate(): String = dateFormatStored.format(Date())

    // Função para registrar que um exercício foi feito na data fornecida
    fun exerciceDone(date: String = getCurrentDate(), exerciceID: Int) {
        val formattedDate = date.replace("/", "-")
        val exercicesList = getExerciceList(exerciceID).toMutableList().apply {
            add("$formattedDate$exerciceID")
        }
        updateCache(exerciceID, exercicesList)
    }

    // Função para registrar que um exercício não foi feito nas últimas `days` dias
    fun exerciceNotDone(date: String = getCurrentDate(), exerciceID: Int, days: Int = 7) {
        val startDate = dateFormatInput.parse(date) ?: return
        val exercicesList = getExerciceList(exerciceID).toMutableList()
        val calendar = Calendar.getInstance().apply { time = startDate }

        repeat(days) {
            val checkDate = dateFormatStored.format(calendar.time)
            exercicesList.remove("$checkDate$exerciceID")
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        updateCache(exerciceID, exercicesList)
    }

    // Função para verificar se um exercício foi feito em algum dos últimos `days` dias
    fun getExercice(date: String = getCurrentDate(), exerciceID: Int, days: Int = 7): Boolean {
        val startDate = dateFormatInput.parse(date) ?: return false
        val calendar = Calendar.getInstance().apply { time = startDate }

        repeat(days) {
            val checkDate = dateFormatStored.format(calendar.time)
            if (getExerciceList(exerciceID).contains("$checkDate$exerciceID")) {
                return true
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return false
    }

    // Função privada para obter a lista de exercícios armazenada para um determinado ID
    private fun getExerciceList(exerciseID: Int): List<String> {
        val cacheKey = "$exerciseID-exercicesList"
        return if (cache.hasCache(context, cacheKey)) {
            val json = cache.getCache(context, cacheKey)
            jsonUtil.fromJson(json, Array<String>::class.java).toList()
        } else {
            emptyList()
        }
    }

    // Função privada para atualizar o cache com a nova lista de exercícios
    private fun updateCache(exerciseID: Int, exercicesList: List<String>) {
        val cacheKey = "$exerciseID-exercicesList"
        cache.setCache(context, cacheKey, jsonUtil.toJson(exercicesList))
    }
}
