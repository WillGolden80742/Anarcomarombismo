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
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class WebHandler {

    companion object {

        private const val YOUTUBE_THUMBNAIL_URL = "https://img.youtube.com/vi/"

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
                val videoId = if (formattedLink.contains("embed/")) {
                    formattedLink.split("embed/")[1]
                } else {
                    ""
                }

                if (formattedLink.isNotEmpty() && isNetworkAvailable(context)) {
                    val cache = Cache()
                    try {
                        if (!cache.hasCache(context, videoId!!)) {
                            val thumbnail = downloadThumbnail(videoId)
                            thumbnail?.let { base64Image ->
                                cache.setCache(context, videoId, base64Image)
                            }
                        }
                    } catch (e: Exception) {
                        println("Erro ao baixar a miniatura com URL: ${e.message}")
                    }
                    webView.loadUrl(formattedLink)
                } else {
                    videoId?.let {
                        val cache = Cache()
                        if (cache.hasCache(context, it)) {
                            try {
                                val cachedThumbnail: String = cache.getCache(context, it, String::class.java)
                                val noInternetIcon = withContext(Dispatchers.IO) {
                                    val inputStream = context.resources.openRawResource(R.raw.no_ethernet)
                                    inputStream.bufferedReader().use { it -> it.readText() }
                                }
                                val html = """
                                <html>
                                <head>
                                    <style>
                                        body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
                                        .cachedThumbnail { 
                                            width: 100%; height: 100%; 
                                            background-size: cover; 
                                            background-position: center; 
                                            position: absolute; 
                                        }
                                        .noInternetIcon { 
                                            position: absolute; 
                                            top: 50%; left: 50%; 
                                            width: 25%; height: 25%; 
                                            background-size: contain; 
                                            background-repeat: no-repeat; 
                                            transform: translate(-50%, -50%); 
                                        }
                                    </style>
                                </head>
                                <body>
                                    <div class="cachedThumbnail" style="background-image:url('data:image/jpeg;base64,$cachedThumbnail');"></div>
                                    <div class="noInternetIcon" style="background-image:url('data:image/svg+xml;base64,$noInternetIcon');"></div>
                                </body>
                                </html>
                            """.trimIndent()

                                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                            } catch (e: Exception) {
                                println("Erro ao carregar miniatura do cache: ${e.message}")
                                loadFallbackBanner(context, webView)
                            }
                        } else {
                            loadFallbackBanner(context, webView)
                        }
                    }
                }
            }
        }

        fun embedVideoForm(context: Context, webView: WebView, formattedLink: String) {
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

        @OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)
        private suspend fun downloadThumbnail(videoId: String): String? {
            return withContext(Dispatchers.IO) {
                try {
                    val url = "$YOUTUBE_THUMBNAIL_URL$videoId/0.jpg"
                    val request = Request.Builder().url(url).build()
                    val client = OkHttpClient()
                    val response = client.newCall(request).execute()

                    if (!response.isSuccessful) {
                        println("Erro ao baixar a miniatura: ${response.message}")
                        return@withContext null
                    }

                    val content = response.body?.bytes() ?: throw IOException("No content received")
                    println("Miniatura baixada com sucesso: ${Base64.encode(content)}")
                    Base64.encode(content)  // Converte os bytes da imagem em Base64
                } catch (e: IOException) {
                    println("Erro ao baixar a miniatura: ${e.message}")
                    null
                }
            }
        }

        private fun loadFallbackBanner(context: Context, webView: WebView) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val text = withContext(Dispatchers.IO) {
                        val inputStream = context.resources.openRawResource(R.raw.vector_banner)
                        inputStream.bufferedReader().use { it.readText() }
                    }
                    webView.loadUrl("data:image/svg+xml;base64,$text")
                } catch (e: Exception) {
                    println("Erro ao carregar banner de fallback: ${e.message}")
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