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

    private val cache = Cache()
    private val jsonUtil = JSON()
    fun searchFood(context: Context, query: String): List<FoodSearch> {
        
        val queryHash = getKey(query)

        if (cache.hasCache(context, queryHash)) {
            println("CACHE HIT for queryHash: $queryHash")
            return getCachedFoodData(context, queryHash)
        } else {
            return fetchAndCacheFoodData(context, query, queryHash)
        }
    }

    private fun getCachedFoodData(context: Context, queryHash: String): List<FoodSearch> {
        val cachedJson = cache.getCache(context, queryHash)
        return jsonUtil.fromJson(cachedJson, Array<FoodSearch>::class.java).toList()
    }

    private fun fetchAndCacheFoodData(context: Context, query: String, queryHash: String): List<FoodSearch> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        val items = mutableListOf<FoodSearch>()

        for (i in 0..1) {
            val url =
                "https://www.fatsecret.com.br/calorias-nutri%C3%A7%C3%A3o/search?q=$encodedQuery&pg=$i"
            try {
                val document = fetchDocument(url)
                val links = document.select("a.prominent")
                val smallTextDivs = document.select("div.smallText")

                for (link in links) {
                    val foodSearch = parseFoodSearch(link, smallTextDivs)
                    if (foodSearch != null) {
                        items.add(foodSearch)
                    }
                }

                cache.setCache(context, queryHash, jsonUtil.toJson(items))
            } catch (e: IOException) {
                println("Erro: ${e.message}")
                return emptyList()
            }
        }

        return items
    }

    private fun fetchDocument(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .get()
    }

    private fun parseFoodSearch(link: Element, smallTextDivs: Elements): FoodSearch? {
        val href = link.attr("href")
        val name = link.text().trim()
        val smallTextContent = findSmallTextContent(link, smallTextDivs)
        val smallTextBeforeDash = smallTextContent.split("-")[0]

        val grams = extractGrams(smallTextBeforeDash)

        return if (grams != null) {
            FoodSearch(
                name = name,
                href = "https://www.fatsecret.com.br$href",
                smallText = smallTextBeforeDash,
                grams = grams.replace("g", "")
            )
        } else {
            null
        }
    }

    private fun findSmallTextContent(link: Element, smallTextDivs: Elements): String {
        for (div in smallTextDivs) {
            if (div.parent() == link.parent()) {
                return div.text().trim()
            }
        }
        return ""
    }

    private fun extractGrams(text: String): String? {
        return Regex("(\\d+\\s*g|\\d+g)").find(text)?.value
    }

    fun getFoodByURL(context: Context, url: String, grams: Double): Food {
        val foodNumber = getKey(url)
        if (cache.hasCache(context, foodNumber)) {
            println("CACHE HIT for foodNumber: $foodNumber")
            return jsonUtil.fromJson(cache.getCache(context, foodNumber), Food::class.java)
        } else {
            try {
                val html = fetchHtmlContent(url)
                val doc: Document = Jsoup.parse(html)

                val foodDescription = extractFoodDescription(doc)
                val nutrients = extractNutrients(doc)
                val food = createFoodObject(foodNumber, grams, foodDescription, nutrients)

                cache.setCache(context, foodNumber, jsonUtil.toJson(food))
                println(JSON().toJson(food))
                return food
            } catch (e: Exception) {
                println("Error Food: ${e.message}")
                return Food()
            }
        }
    }

    private fun fetchHtmlContent(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) throw Exception("Failed to fetch data")

        return response.body?.string() ?: throw Exception("No content received")
    }

    private fun extractFoodDescription(doc: Document): String {
        var foodDescription = doc.select("h1[style='text-transform:none']").text().trim()
        return foodDescription.replace(Regex("\\(\\d+g\\)", RegexOption.IGNORE_CASE), "").trim()
    }
    private fun extractNutrients(doc: Document): MutableMap<String, String> {
        val nutrients = mutableMapOf<String, String>()
        doc.select("div.nutrient.left").forEach { element: Element ->
            val keyText = element.text().trim()
            val value = element.nextElementSibling()?.text()?.replace(',', '.')?.trim() ?: "0"
            nutrients[keyText] = value
        }
        return nutrients
    }

    private fun convertKjToKcal(kj: Double): Double {
        return kj / 4.184
    }

    private fun createFoodObject(
        foodNumber: String,
        grams: Double,
        foodDescription: String,
        nutrients: MutableMap<String, String>
    ): Food {
        val energyKj = nutrients["Energia"]?.replace(Regex("[^0-9,.]"), "")?.replace(',', '.')
            ?.toDoubleOrNull() ?: 0.0
        val energyKcal = convertKjToKcal(energyKj)

        return Food(
            foodNumber = "web$foodNumber",
            grams = 100.0,
            foodDescription = "$foodDescription ᯤ",
            energyKcal = formatNutrientValue(energyKcal, grams),
            energyKj = formatNutrientValue(energyKj, grams),
            protein = formatNutrientValue(nutrients["Proteínas"], grams),
            lipids = formatNutrientValue(nutrients["Gorduras"], grams),
            cholesterol = formatNutrientValue(nutrients["Colesterol"], grams),
            carbohydrate = formatNutrientValue(nutrients["Carboidratos"], grams),
            dietaryFiber = formatNutrientValue(nutrients["Fibras"], grams),
            sodium = formatNutrientValue(nutrients["Sódio"], grams),
            potassium = formatNutrientValue(nutrients["Potássio"], grams)
        )
    }

    private fun formatNutrientValue(nutrientValue: String?, grams: Double): String {
        val value = nutrientValue?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
        return DecimalFormat("#.##").format((value / grams) * 100.0).replace(",", ".")
    }

    private fun formatNutrientValue(value: Double, grams: Double): String {
        return DecimalFormat("#.##").format((value / grams) * 100.0).replace(",", ".")
    }

    private fun getKey(value: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val hash = BigInteger(1, md.digest(value.toByteArray()))
            hash.toString(16) + value.length
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}
