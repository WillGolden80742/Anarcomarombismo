package com.example.anarcomarombismo.Controller.Util

import com.google.gson.Gson
import com.google.gson.JsonParser

class JSON {
    companion object {
        fun <T> fromJson(json: String, classOfT: Class<T>): T {
            val gson = Gson()
            return gson.fromJson(json, classOfT)
        }

        fun toJson(obj: Any): String {
            val gson = Gson()
            return gson.toJson(obj)
        }

        fun hasAttribute(json: String, attribute: String): Boolean {
            val jsonObject = JsonParser.parseString(json).asJsonObject
            return jsonObject.has(attribute)
        }

    }
}
