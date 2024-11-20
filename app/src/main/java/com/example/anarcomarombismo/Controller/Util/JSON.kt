package com.example.anarcomarombismo.Controller.Util

import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
        @OptIn(ExperimentalEncodingApi::class)
        fun toCompressedJson(content:Any): String {
            val json = toJson(content)
            try {
                val baos = ByteArrayOutputStream()
                GZIPOutputStream(baos).bufferedWriter(Charsets.UTF_8).use { it.write(json) }
                val compressedBytes = baos.toByteArray()
                return Base64.encode(compressedBytes)
            } catch (e: Exception) {
                throw CompressionException("Failed to compress JSON", e)
            }
        }
        @OptIn(ExperimentalEncodingApi::class)
        fun <T> fromCompressedJson(compressedJson: String, classOfT: Class<T>): T {
            return try {
                val decodedBytes = Base64.decode(compressedJson)
                val bais = ByteArrayInputStream(decodedBytes)
                val json = GZIPInputStream(bais).bufferedReader(Charsets.UTF_8).use { it.readText() }
                println("json not deserialized : $json")
                fromJson(json, classOfT)
            } catch (e: Exception) {
                throw CompressionException("Failed to decompress JSON", e)
            }
        }

        class CompressionException(message: String, cause: Throwable? = null) : Exception(message, cause)

    }
}
