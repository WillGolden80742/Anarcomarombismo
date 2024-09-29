package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache

class MacroTarget (
    var calories: Double = 2000.0,
    var lipids: Double = 44.44,
    var carbs: Double = 250.0,
    val protein: Double = 150.0,
    val dietaryFiber: Double = 20.0,
) : DataHandler<MacroTarget> {

    companion object {
        private var cache = Cache();
        private const val cacheKey = "MacroTarget"
        fun build (calories: Double = 2000.0, lipids: Double = 44.44, carbs: Double = 250.0, protein: Double = 150.0,dietaryFiber: Double = 20.0): MacroTarget {
            return MacroTarget(calories, lipids, carbs, protein, dietaryFiber)
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

    override fun fetchById(context: Context, id: Any): MacroTarget? {
        return if (cache.hasCache(context,cacheKey)) {
            fetchAll(context).first()
        } else {
            MacroTarget()
        }
    }

    fun fetchById(context: Context): MacroTarget? {
        return fetchById(context,0)
    }
    override fun fetchAll(context: Context): List<MacroTarget> {
        return if (cache.hasCache(context,cacheKey)) {
            cache.getCache(context, cacheKey, Array<MacroTarget>::class.java).toList()
        } else {
            listOf(MacroTarget())
        }
    }
}