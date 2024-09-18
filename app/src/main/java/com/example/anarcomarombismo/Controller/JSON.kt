package com.example.anarcomarombismo.Controller

import com.google.gson.Gson

class JSON {
    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        val gson = Gson()
        return gson.fromJson(json, classOfT)
    }
    fun toJson(obj: Any): String {
        val gson = Gson()
        return gson.toJson(obj)
    }
}
