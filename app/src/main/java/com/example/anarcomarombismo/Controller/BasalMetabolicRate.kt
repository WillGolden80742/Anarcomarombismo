package com.example.anarcomarombismo.Controller

import android.content.Context
import com.example.anarcomarombismo.Controller.Interface.DataHandler
import com.example.anarcomarombismo.Controller.Util.Cache

class BasalMetabolicRate (
    val weight: Double = 70.0,
    val height: Int = 165,
    val age: Int = 30,
    val gender: String = "",
    val activityLevel: Double = 1.55
): DataHandler<BasalMetabolicRate> {
    companion object {
        private var cache = Cache()
        private var cacheKey = "BasalMetabolicRate"
        fun build(weight: Double, height: Int, age: Int, gender: String, activityLevel: Double): BasalMetabolicRate {
            return BasalMetabolicRate(weight, height, age, gender, activityLevel)
        }
    }
    override fun save(context: Context): Boolean {
        cache.setCache(context, cacheKey, listOf(this))
        return true
    }

    override fun remove(context: Context): Boolean {
        return false
    }

    override fun fetchById(context: Context, id: Any): BasalMetabolicRate? {
        return fetchAll(context).first()
    }

    fun fetch (context: Context): BasalMetabolicRate? {
        return fetchById(context,0)
    }

    fun hasBasalMetabolicRate(context: Context): Boolean {
        return cache.hasCache(context, cacheKey)
    }

    fun getBasalMetabolicRate(): Double {
        return if (gender == "M") {
            ((9.99 * weight + 6.25 * height - 4.92 * age + 5)*activityLevel).toInt().toDouble()
        } else {
            ((9.99 * weight + 6.25 * height - 4.92 * age - 161)*activityLevel).toInt().toDouble()
        }
    }

    override fun toString(): String {
        // calculate BMR consider M man and F woman (9,99 * Peso + 6,25 * Altura - 4,92 * Idade + 5) * FA to Man and (9.99 x Peso + 6.25 x Altura – 4.92 x Idade -161) to Woman
        return "Metaboismo Basal: "+getBasalMetabolicRate().toString()
    }

    override fun fetchAll(context: Context): List<BasalMetabolicRate> {
        return if (cache.hasCache(context, cacheKey)) {
            cache.getCache(context, cacheKey,Array<BasalMetabolicRate>::class.java).toList()
        } else {
            listOf(BasalMetabolicRate())
        }
    }
}