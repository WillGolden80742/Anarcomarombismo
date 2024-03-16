package com.example.anarcomarombismo.Controller

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Cache {

    companion object {
        private const val CACHE_DIRECTORY = "Cache/"
    }


    private var fileName: String? = null
    private var file: File? = null

    private fun getHashMd5(value: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        val hash = BigInteger(1, md.digest(value.toByteArray()))
        return hash.toString(16)
    }

    private fun fileHashed(fileName: String) {
        this.fileName = getHashMd5(fileName) + ".json"
    }

    fun setCache(context: Context, nomeArquivo: String, texto: String) {
        fileHashed(nomeArquivo)
        file = File(context.filesDir, CACHE_DIRECTORY + fileName)

        if (file!!.exists()) {
            try {
                val writer = FileOutputStream(file)
                writer.write(texto.toByteArray())
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            writeToFile(context, fileName!!, texto.toByteArray())
        }
    }

    fun getCache(context: Context,nomeArquivo: String): String {
        fileHashed(nomeArquivo)
        file = File(context.filesDir, CACHE_DIRECTORY + fileName)
        return if (file!!.exists() && file!!.length() > 0) {
            file!!.readText()
        } else {
            "NOT_FOUND"
        }
    }

    // directory is \res\json
    fun getCacheByRes(context: Context, fileName: String): String {
        val resourceId = context.resources.getIdentifier(fileName, "raw", context.packageName)
        return context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }

    fun hasCache(context: Context,nomeArquivo: String): Boolean {
        fileHashed(nomeArquivo)
        file = File(context.filesDir, CACHE_DIRECTORY + fileName)
        return file!!.exists() && file!!.length() > 0
    }

    private fun writeToFile(context: Context, fileName: String, content: ByteArray) {
        val path = File(context.filesDir, CACHE_DIRECTORY)
        val newDir = File(path.toString())
        try {
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            val writer = FileOutputStream(File(path, fileName))
            writer.write(content)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
