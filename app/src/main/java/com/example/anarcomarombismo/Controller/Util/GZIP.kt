package com.example.anarcomarombismo.Controller.Util

import android.annotation.SuppressLint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class GZIP {
    companion object {
        @SuppressLint("NewApi")
        fun compressText (text: String): String {
            return if (text.length < 250) {
                text
            } else {
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)
                    gzipOutputStream.write(text.toByteArray(Charsets.UTF_8))
                    gzipOutputStream.close()
                    Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
                } catch (e: Exception) {
                    throw CompressionException("Erro ao comprimir o texto: ${e.message}")
                }
            }
        }
        @SuppressLint("NewApi")
        fun decompressText(compressedBase64: String): String {
            return try {
                val base64Regex = Regex("^[A-Za-z0-9+/]+={0,2}\$").containsMatchIn(compressedBase64) && compressedBase64.length > 2
                if (base64Regex) {
                    val sanitizedBase64 = compressedBase64.replace("\\s".toRegex(), "")
                    val decodedBytes = Base64.getDecoder().decode(sanitizedBase64)
                    val gzipInputStream = GZIPInputStream(ByteArrayInputStream(decodedBytes))
                    val decompressedBytes = gzipInputStream.readBytes()
                    String(decompressedBytes, Charsets.UTF_8)
                } else {
                    compressedBase64
                }
            } catch (e: Exception) {
                throw Exception("Erro ao descomprimir o texto: ${e.message}")
                compressedBase64
            }
        }

        private class CompressionException(message: String, cause: Throwable? = null) : Exception(message, cause)

    }
}