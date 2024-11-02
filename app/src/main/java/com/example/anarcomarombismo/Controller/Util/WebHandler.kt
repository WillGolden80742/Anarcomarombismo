package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.WebView
import com.example.anarcomarombismo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class WebHandler {

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

        fun embedVideo(context: Context, webView: WebView, formattedLink: String) {
            CoroutineScope(Dispatchers.Main).launch {
                if (formattedLink.isNotEmpty() && isNetworkAvailable(context)) {
                    webView.loadUrl(formattedLink)
                } else {
                    val text = withContext(Dispatchers.IO) {
                        val inputStream = context.resources.openRawResource(R.raw.vector_banner)
                        inputStream.bufferedReader().use { it.readText() }
                    }
                    webView.loadUrl("data:image/svg+xml;base64,$text")
                }
            }
        }
        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }

        fun generateYouTubeEmbedLink(text: String): String {
            val trimmedText = text.trim()

            if (trimmedText.contains("youtube.com/embed/")) {
                return trimmedText
            }

            if (!isValidYouTubeLink(trimmedText)) {
                return ""
            }

            val sanitizedText = removeUnnecessaryParameters(trimmedText)
            val videoId = extractVideoId(sanitizedText) ?: return ""

            return buildEmbedLink(videoId, sanitizedText)
        }

        private fun buildEmbedLink(videoId: String, text: String): String {
            val timeParameter = extractTimeParameter(text)
            return "https://www.youtube.com/embed/$videoId$timeParameter"
        }

        private fun extractTimeParameter(text: String): String {
            val time = text.substringAfter("&t=", "").takeIf { it.isNotEmpty() }
            return time?.let { "?t=$it" } ?: ""
        }

        private fun isValidYouTubeLink(text: String): Boolean {
            return text.contains("youtu.be") || text.contains("youtube")
        }


        private fun removeUnnecessaryParameters(text: String): String {
            return text.replace(Regex("[&?](feature=youtu\\.be|si=.*)"), "")
        }

        private fun extractVideoId(text: String): String? {
            return when {
                text.contains("/live/") -> text.substringAfter("/live/").substringBefore("?")
                text.contains("/shorts/") -> text.substringAfter("/shorts/").substringBefore("?")
                text.contains("watch?v=") -> text.substringAfter("watch?v=").substringBefore("&")
                text.contains("youtu.be/") -> text.substringAfter("youtu.be/").substringBefore("?")
                else -> null
            }
        }
    }
}