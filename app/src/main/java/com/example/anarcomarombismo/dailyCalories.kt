package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.JSON
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import com.example.anarcomarombismo.Adapters.DailyCaloriesAdapter
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.DailyCalories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class dailyCalories : AppCompatActivity() {
    private lateinit var caloriesFoodList: ListView
    private lateinit var addCaloriesButton: Button
    private lateinit var addNewFoodButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories)
        caloriesFoodList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addFoodFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        setDailyCaloriesList()
        addCaloriesButton.setOnClickListener {
            callFormDailyCalories()
        }

        caloriesFoodList.setOnItemClickListener { parent, view, position, id ->
            val dailyCalories = parent.getItemAtPosition(position) as DailyCalories
            val intent = Intent(this, formDailyCalories::class.java)
            try {
                intent.putExtra("dailyCaloriesDate", dailyCalories.date)
                startActivity(intent)
            } catch (e: Exception) {
                println("Erro ao chamar a tela de calorias diárias: $e")
            }
        }
        addNewFoodButton.setOnClickListener {
            callFoodForm()
        }
    }
    // onResume
    override fun onResume() {
        super.onResume()
        setDailyCaloriesList()
    }

    fun callFormDailyCalories() {
        try {
            startActivity(Intent(this, formDailyCalories::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    fun callFoodForm() {
        try {
            startActivity(Intent(this, formFoods::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de alimentos: $e")
        }
    }

    fun setDailyCaloriesList() {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = Cache()
            val jsonUtil = JSON()
            var dailyCaloriesList: List<DailyCalories> = emptyList() // Inicialize a lista como vazia
            try {
                // Verifique se o cache não é nulo antes de acessá-lo
                if (cache != null && cache.hasCache(this@dailyCalories, "dailyCalories")) {
                    // Obtenha a lista de calorias diárias do cache
                    val dailyCaloriesListJson = cache.getCache(this@dailyCalories, "dailyCalories")
                    println("Lista de calorias diárias: $dailyCaloriesListJson")
                    dailyCaloriesList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
                } else {
                    val dailyCaloriesListJson = cache.getCache(this@dailyCalories, "emptyDailyCalories")
                    println("Lista de calorias diárias: $dailyCaloriesListJson")
                    dailyCaloriesList = jsonUtil.fromJson(dailyCaloriesListJson, Array<DailyCalories>::class.java).toList()
                }
                // Ordenar a lista por data (ano -> mês -> dia)
                dailyCaloriesList = dailyCaloriesList.sortedByDescending { dailyCalories ->
                    val dateParts = dailyCalories.date.split("/")
                    // Converter a data para o formato YYYYMMDD para ordenação correta
                    "${dateParts[2]}${dateParts[1]}${dateParts[0]}".toInt()
                }
                // Atualize a UI na thread principal
                launch(Dispatchers.Main) {
                    // Crie um adapter para a lista de objetos DailyCalories
                    val adapter = DailyCaloriesAdapter(this@dailyCalories, dailyCaloriesList)
                    // Atribua o adapter à lista de alimentos
                    caloriesFoodList.adapter = adapter
                }
            } catch (e: Exception) {
                println("Erro ao carregar a lista de calorias diárias: $e")
            }
        }
    }


}