package com.example.anarcomarombismo.Controller.Interface

import android.content.Context

interface PersistentData<T> {
    fun save(context: Context): Boolean
    fun remove(context: Context): Boolean
    fun load(context: Context,id:Any): T?
    fun loadList(context: Context): List<T>

}