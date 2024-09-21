package com.example.anarcomarombismo.Controller.Util

import java.text.Normalizer

class StringHandler {
     private fun normalizeString(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
    }
     fun containsQuery(text: String, query: String): Boolean {
        if (query.isEmpty()) {
            return true
        }
        val normalizedText = normalizeString(text)
        val normalizedQuery = normalizeString(query)
        for (word in normalizedQuery.split(" ")) {
            if (normalizedText.contains(word, ignoreCase = true) && word.length > 2) {
                return true
            }
        }
        return false
    }
}