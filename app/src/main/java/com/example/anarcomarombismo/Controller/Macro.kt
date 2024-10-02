package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache

class Macro (
    var calories: Double = 1960.0,
    var lipids: Double = 42.0,
    var lipidsByWeight: Double = 0.6,
    var carbs: Double = 255.5,
    var protein: Double = 140.0,
    var proteinByWeight: Double = 2.0,
    var dietaryFiber: Double = 20.0,
) : DataHandler<Macro> {

    companion object {
        private var cache = Cache();
        private const val cacheKey = "Macro"
        fun build(calories: Double, lipids: Double, lipidsByWeight: Double, carbs: Double, protein: Double, proteinByWeight: Double, dietaryFiber: Double): Macro {
            return Macro(
                calories = calories,
                lipids = lipids,
                lipidsByWeight = lipidsByWeight,
                carbs = carbs,
                protein = protein,
                proteinByWeight = proteinByWeight,
                dietaryFiber = dietaryFiber
            )
        }

    }
    override fun save(context: Context): Boolean {
        cache.setCache(context,cacheKey,listOf(this))
        return true

    }

    override fun remove(context: Context): Boolean {
        cache.setCache(context,cacheKey,listOf(null))
        return true
    }

    override fun fetchById(context: Context, id: Any): Macro? {
        return if (cache.hasCache(context,cacheKey)) {
            fetchAll(context).first()
        } else {
            Macro()
        }
    }

    fun fetch(context: Context): Macro? {
        return fetchById(context,0)
    }
    override fun fetchAll(context: Context): List<Macro> {
        return if (cache.hasCache(context,cacheKey)) {
            cache.getCache(context, cacheKey, Array<Macro>::class.java).toList()
        } else {
            listOf(Macro())
        }
    }
}