package com.example.anarcomarombismo.Controller.Interface

import android.content.Context

interface DataHandler<T> {
    fun save(context: Context): Boolean
    fun remove(context: Context): Boolean
    fun fetchById(context: Context, id:Any): T?
    fun fetchAll(context: Context): List<T>
}