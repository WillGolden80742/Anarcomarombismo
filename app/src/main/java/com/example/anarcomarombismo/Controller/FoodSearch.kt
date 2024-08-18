package com.example.anarcomarombismo.Controller

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat

class FoodSearch (var name:String = "", var href:String = "",var smallText:String = "", var grams:String = "") {
    fun getFoodByURL(context: Context, url: String, grams: Double): Food {
        val cache = Cache()
        val jsonUtil = JSON()
        val foodNumber = getKey(url)
        if (cache.hasCache(context, foodNumber)) {
            println("CACHE HIT for foodNumber: $foodNumber")
            return jsonUtil.fromJson(cache.getCache(context, foodNumber), Food::class.java)
        } else {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Failed to fetch data")

                val html = response.body?.string() ?: throw Exception("No content received")
                val doc: Document = Jsoup.parse(html)

                // Extract food description
                var foodDescription = doc.select("h1[style='text-transform:none']").text().trim()
                foodDescription = foodDescription.replace(Regex("\\(\\d+g\\)", RegexOption.IGNORE_CASE), "").trim()
                // Extract nutrients
                val nutrients = mutableMapOf<String, String>()
                doc.select("div.nutrient.left").forEach { element: Element ->
                    val keyText = element.text().trim()
                    val value = element.nextElementSibling()?.text()?.replace(',', '.')?.trim() ?: "0"
                    nutrients[keyText] = value
                }

                // Convert Kj to Kcal
                fun convertKjToKcal(kj: Double): Double = kj / 4.184

                // Extract energy and convert
                val energyKj = nutrients["Energia"]?.replace(Regex("[^0-9,.]"), "")?.replace(',', '.')
                    ?.toDoubleOrNull() ?: 0.0
                val energyKcal = convertKjToKcal(energyKj)

                // Generate a foodNumber with a random number and timestamp

                // Create the Food object and normalize the values
                val food = Food(
                    foodNumber = foodNumber,
                    grams = 100.0,
                    foodDescription = foodDescription,
                    energyKcal = DecimalFormat("#.##").format((energyKcal / grams) * 100.0).replace(",", "."),
                    energyKj = DecimalFormat("#.##").format((energyKj / grams) * 100.0).replace(",", "."),
                    protein = DecimalFormat("#.##").format((nutrients["Proteínas"]?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    lipids = DecimalFormat("#.##").format((nutrients["Gorduras"]?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    cholesterol = DecimalFormat("#.##").format((nutrients["Colesterol"]?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    carbohydrate = DecimalFormat("#.##").format((nutrients["Carboidratos"]?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    dietaryFiber = DecimalFormat("#.##").format((nutrients["Fibras"]?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    sodium = DecimalFormat("#.##").format((nutrients["Sódio"]?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", "."),
                    potassium = DecimalFormat("#.##").format((nutrients["Potássio"]?.replace(Regex("[^0-9]"), "")?.toDoubleOrNull() ?: 0.0) / grams * 100.0).replace(",", ".")
                )
                cache.setCache(context, foodNumber, jsonUtil.toJson(food))
                println(JSON().toJson(food))
                return food
            } catch (e: Exception) {
                println("Error Food: ${e.message}")
                return Food()
            }
        }
    }

    fun fetchFoodData(context: Context,query: String): List<FoodSearch> {
        val cache = Cache()
        val jsonUtil = JSON()
        val queryHash = getKey(query)
        var items = mutableListOf<FoodSearch>()
        if (cache.hasCache(context, queryHash)) {
            println("CACHE HIT for queryHash: $queryHash")
            items = jsonUtil.fromJson(cache.getCache(context, queryHash), Array<FoodSearch>::class.java).toMutableList()
            items = items.sortedBy { it.name }.toMutableList()
            return items
        } else {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url =
                "https://www.fatsecret.com.br/calorias-nutri%C3%A7%C3%A3o/search?q=$encodedQuery"

            try {
                val document: Document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get()

                val links: Elements = document.select("a.prominent")
                val smallTextDivs: Elements = document.select("div.smallText")

                for (link in links) {
                    val href = link.attr("href")
                    val name = link.text().trim()
                    var smallTextContent = ""

                    for (div in smallTextDivs) {
                        if (div.parent() == link.parent()) {
                            smallTextContent = div.text().trim()
                            break
                        }
                    }

                    val smallTextBeforeDash = smallTextContent.split("-")[0]

                    val grams: String? = Regex("(\\d+\\s*g|\\d+g)").find(smallTextBeforeDash)?.value

                    if (grams != null) {
                        items.add(
                            FoodSearch(
                                name = name,
                                href = "https://www.fatsecret.com.br$href",
                                smallText = smallTextBeforeDash,
                                grams = grams.replace("g", "")
                            )
                        )
                    }
                }
                cache.setCache(context, queryHash, jsonUtil.toJson(items))
            } catch (e: IOException) {
                println("Erro: ${e.message}")
                // Retorna uma lista vazia em caso de erro
                return emptyList()
            }
            items = items.sortedBy { it.name }.toMutableList()
            return items
        }
    }

    private fun getKey(value: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }

        val hash = BigInteger(1, md.digest(value.toByteArray()))
        return hash.toString(16)+value.length
    }

}
