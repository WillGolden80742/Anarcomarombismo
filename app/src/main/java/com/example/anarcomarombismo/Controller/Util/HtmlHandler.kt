package com.example.anarcomarombismo.Controller.Util

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlHandler {

    companion object {
        fun fetchHtmlContent(url: String): String {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("Failed to fetch data")
            return response.body?.string() ?: throw Exception("No content received")
        }

        fun fetchDocument(url: String): Document {
            return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()
        }
    }
}