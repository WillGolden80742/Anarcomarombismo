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
        private fun isValidBase64(base64: String): Boolean {
            val base64Regex = Regex("^[A-Za-z0-9+/]+={0,2}$")
            return base64Regex.matches(base64) && base64.length % 4 == 0
        }
        @SuppressLint("NewApi")
        fun decompressText(compressedBase64: String): String {
            return try {
                if (isValidBase64(compressedBase64)) {
                    val sanitizedBase64 = compressedBase64.replace("\\s".toRegex(), "")
                    val decodedBytes = Base64.getDecoder().decode(sanitizedBase64)
                    val gzipInputStream = GZIPInputStream(ByteArrayInputStream(decodedBytes))
                    val decompressedBytes = gzipInputStream.readBytes()
                    String(decompressedBytes, Charsets.UTF_8)
                } else {
                    compressedBase64
                }
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Erro ao processar Base64: ${e.message}")
            } catch (e: Exception) {
                throw Exception("Erro ao descomprimir o texto: ${e.message}")
            }
        }

        private class CompressionException(message: String, cause: Throwable? = null) : Exception(message, cause)

    }
}