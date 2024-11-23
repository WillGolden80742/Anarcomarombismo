package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Cache {

    companion object {
        private const val CACHE_DIRECTORY = "Cache/"
        private const val SHA1_ALGORITHM = "SHA-1"
        private const val MD5_ALGORITHM = "MD5"
        private const val NOT_FOUND = "NOT_FOUND"
        private const val JSON_EXTENSION = ".json"
    }

    private fun setCacheText(context: Context, fileName: String, text: String) {
        val hashedFileName = hashFileName(fileName, SHA1_ALGORITHM)
        val compressText = GZIP.compressText(text)
        writeToFile(context, hashedFileName, compressText)
    }

    private fun getCacheText(context: Context, fileName: String): String {
        val sha1HashedFileName = hashFileName(fileName, SHA1_ALGORITHM)
        val md5HashedFileName = hashFileName(fileName, MD5_ALGORITHM)

        val compressText = getCacheContent(context, sha1HashedFileName)
            ?: getCacheContent(context, md5HashedFileName)
            ?: NOT_FOUND
        return GZIP.decompressText(compressText)
    }


    fun setCache(context: Context, fileName: String, obj: Any) {
        setCacheText(context, fileName,JSON.toJson(obj))
    }

    fun <T> getCache(context: Context, fileName: String, clazz: Class<T>): T {
       return JSON.fromJson(getCacheText(context, fileName),clazz)
    }
    fun hasCache(context: Context, fileName: String): Boolean {
        val sha1HashedFileName = hashFileName(fileName, SHA1_ALGORITHM)
        val md5HashedFileName = hashFileName(fileName, MD5_ALGORITHM)
        return fileExists(context, sha1HashedFileName) || fileExists(context, md5HashedFileName)
    }

    private fun hashFileName(fileName: String, algorithm: String): String {
        val hash = hashString(fileName, algorithm)
        return if (algorithm == SHA1_ALGORITHM) {
            "$hash${fileName.length}$JSON_EXTENSION"
        } else {
            "$hash$JSON_EXTENSION"
        }
    }

    private fun hashString(input: String, algorithm: String): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalArgumentException("Invalid hashing algorithm: $algorithm", e)
        }
    }

    private fun writeToFile(context: Context, fileName: String, content: String) {
        try {
            val file = getFile(context, fileName)
            file.parentFile?.mkdirs()
            file.writeText(content)
        } catch (e: IOException) {
            throw CacheException("Failed to write to cache file: $fileName", e)
        }
    }

    private fun getCacheContent(context: Context, fileName: String): String? {
        val file = getFile(context, fileName)
        return if (file.exists() && file.length() > 0) {
            try {
                file.readText()
            } catch (e: IOException) {
                throw CacheException("Failed to read cache file: $fileName", e)
            }
        } else {
            null
        }
    }

    private fun fileExists(context: Context, fileName: String): Boolean {
        val file = getFile(context, fileName)
        return file.exists() && file.length() > 0
    }

    private fun getFile(context: Context, fileName: String): File {
        return File(context.filesDir, "$CACHE_DIRECTORY$fileName")
    }

    class CacheException(message: String, cause: Throwable? = null) : Exception(message, cause)
}