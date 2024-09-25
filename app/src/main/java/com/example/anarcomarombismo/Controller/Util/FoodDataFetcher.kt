package com.example.anarcomarombismo.Controller.Util

import android.content.Context
import com.example.anarcomarombismo.Controller.Food
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

class FoodDataFetcher(var name: String = "", var href: String = "", var grams: String = "") {
    private val cache = Cache()
    private val json = JSON()
    companion object {
        private fun build(
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
                sodium = formatNutrientValue(nutrients["Sódio"], grams)
            )
        }
        private fun convertKjToKcal(kj: Double): Double {
            return kj / 4.184
        }
        private fun formatNutrientValue(nutrientValue: String?, grams: Double): String {
            val value = nutrientValue?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
            return DecimalFormat("#.##").format((value / grams) * 100.0).replace(",", ".")
        }
        private fun formatNutrientValue(value: Double, grams: Double): String {
            return DecimalFormat("#.##").format((value / grams) * 100.0).replace(",", ".")
        }
    }
    fun searchFood(context: Context, query: String): List<FoodDataFetcher> {
        val queryHash = getKey(query)
        if (cache.hasCache(context, queryHash)) {
            println("CACHE HIT for queryHash: $queryHash")
            return cache.getCache(context, queryHash,Array<FoodDataFetcher>::class.java).toList()
        }
        return fetchAndCacheOfFoodSearch(context, query, queryHash)
    }
    private fun fetchAndCacheOfFoodSearch(context: Context, query: String, queryHash: String): List<FoodDataFetcher> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val items = mutableListOf<FoodDataFetcher>()
        for (i in 0..1) {
            val url =
                "https://www.fatsecret.com.br/calorias-nutri%C3%A7%C3%A3o/search?q=$encodedQuery&pg=$i"
            try {
                val document = HtmlHandler.fetchDocument(url)
                val links = document.select("a.prominent")
                val smallTextDivs = document.select("div.smallText")

                for (link in links) {
                    val foodSearch = parseFoodSearch(link, smallTextDivs)
                    if (foodSearch != null && StringHandler.containsQuery(foodSearch.name,query)) {
                        items.add(foodSearch)
                    }
                }
            } catch (e: IOException) {
                println("Error: ${e.message}")
                return emptyList()
            }
        }
        cache.setCache(context, queryHash,items)
        return items
    }
    private fun parseFoodSearch(link: Element, smallTextDivs: Elements): FoodDataFetcher? {
        val href = link.attr("href")
        val name = link.text().trim()
        val smallTextContent = findSmallTextContent(link, smallTextDivs)
        val smallTextBeforeDash = smallTextContent.split("-")[0]
        val grams = extractGrams(smallTextBeforeDash)
        return if (grams != null) {
            FoodDataFetcher(
                name = name,
                href = "https://www.fatsecret.com.br$href",
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
            return cache.getCache(context, foodNumber, Food::class.java)
        }
        return try {
            val html = HtmlHandler.fetchHtmlContent(url)
            val doc: Document = Jsoup.parse(html)

            val foodDescription = extractFoodDescription(doc)
            val nutrients = extractNutrients(doc)
            val food = build(foodNumber, grams, foodDescription, nutrients)
            cache.setCache(context, foodNumber, food)
            println(JSON.toJson(food))
            food
        } catch (e: Exception) {
            println("Error Food: ${e.message}")
            Food()
        }
    }
    private fun extractFoodDescription(doc: Document): String {
        val foodDescription = doc.select("div.summarypanelcontent h1").text().trim()
        return foodDescription.replace(Regex("\\d+g", RegexOption.IGNORE_CASE), "").replace("()", "").trim()
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
