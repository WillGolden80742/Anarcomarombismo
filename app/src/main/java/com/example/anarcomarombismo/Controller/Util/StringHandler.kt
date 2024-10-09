package com.example.anarcomarombismo.Controller.Util

import java.text.Normalizer
import java.util.Locale

class StringHandler {
    companion object {
        fun containsQuery(text: String, query: String): Boolean {
            if (query.isBlank()) return true

            val normalizedText = normalizeString(text)
            val normalizedQuery = normalizeString(query)

            val queryWords = normalizedQuery.split(" ").filter { it.length > 2 }
            return if (queryWords.isNotEmpty()) {
                queryWords.all { word -> normalizedText.contains(word, ignoreCase = true) }
            } else {
                normalizedText.contains(normalizedQuery, ignoreCase = true)
            }
        }
        private fun normalizeString(text: String): String {
            return Normalizer.normalize(text.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        }

    }
}