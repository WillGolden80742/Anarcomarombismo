package com.example.anarcomarombismo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.anarcomarombismo.Controller.Adapter.DailyCaloriesAdapter
import com.example.anarcomarombismo.Controller.DailyCalories
import com.example.anarcomarombismo.Controller.Macro
import com.example.anarcomarombismo.Controller.Util.JSON
import com.example.anarcomarombismo.Forms.formDailyCalories
import com.example.anarcomarombismo.Forms.formFood
import com.example.anarcomarombismo.Forms.formMacro
import com.google.android.material.floatingactionbutton.FloatingActionButton

class dailyCalories : AppCompatActivity() {
    private lateinit var exportDailyCalories: FloatingActionButton
    private lateinit var caloriesFoodList: RecyclerView
    private lateinit var addCaloriesButton: FloatingActionButton
    private lateinit var addNewFoodButton: FloatingActionButton
    private lateinit var progressBarContainer: LinearLayout
    private lateinit var editStatisticsButton: Button
    private lateinit var caloriesProgressBar: ProgressBar
    private lateinit var carbsProgressBar: ProgressBar
    private lateinit var fatsProgressBar: ProgressBar
    private lateinit var proteinsProgressBar: ProgressBar
    private lateinit var dietaryFiberProgressBar: ProgressBar
    private lateinit var caloriesLabel: TextView
    private lateinit var carbsLabel: TextView
    private lateinit var lipidsLabel: TextView
    private lateinit var proteinsLabel: TextView
    private lateinit var dietaryFiberLabel: TextView

    private fun initializeUIComponents() {
        exportDailyCalories = findViewById(R.id.exportDailyCalories)
        caloriesFoodList = findViewById(R.id.caloriesFoodList)
        addCaloriesButton = findViewById(R.id.addFoodFormButton)
        addNewFoodButton = findViewById(R.id.addNewFoodButton)
        progressBarContainer = findViewById(R.id.progressBarContainer)
        editStatisticsButton = findViewById(R.id.editStatisticsButton)
        caloriesProgressBar = findViewById(R.id.caloriesProgressBar)
        carbsProgressBar = findViewById(R.id.carbsProgressBar)
        fatsProgressBar = findViewById(R.id.fatsProgressBar)
        proteinsProgressBar = findViewById(R.id.proteinsProgressBar)
        dietaryFiberProgressBar = findViewById(R.id.dietaryFiberProgressBar)
        caloriesLabel = findViewById(R.id.caloriesLabel)
        carbsLabel = findViewById(R.id.carbsLabel)
        lipidsLabel = findViewById(R.id.lipidsLabel)
        proteinsLabel = findViewById(R.id.proteinsLabel)
        dietaryFiberLabel = findViewById(R.id.dietaryFiberLabel)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_calories)
        initializeUIComponents()

        exportDailyCalories.setOnClickListener {
            DailyCalories.export(this)
        }

        addCaloriesButton.setOnClickListener {
            callFormDailyCalories()
        }

        progressBarContainer.setOnClickListener {
            val intent = Intent(this, formMacro::class.java)
            startActivity(intent)
        }

        editStatisticsButton.setOnClickListener {
            val intent = Intent(this, formMacro::class.java)
            startActivity(intent)
        }

        // Initialize the new paginated adapter
        val dailyCaloriesAdapter = DailyCaloriesAdapter(this, clickDailyCalories())
        caloriesFoodList.layoutManager = LinearLayoutManager(this)
        caloriesFoodList.adapter = dailyCaloriesAdapter

        // Add scroll listener for pagination
        caloriesFoodList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Check if we can scroll vertically and if there are more pages
                if (!recyclerView.canScrollVertically(1) &&
                    (caloriesFoodList.adapter as DailyCaloriesAdapter).hasMorePages()) {
                    (caloriesFoodList.adapter as DailyCaloriesAdapter).loadNextPage()
                }
            }
        })

        // Load initial data
        val dailyCaloriesList = DailyCalories().fetchAll(this).ifEmpty { listOf(DailyCalories()) }
        dailyCaloriesAdapter.loadData(dailyCaloriesList)

        addNewFoodButton.setOnClickListener {
            callFoodForm()
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndUpdateMacroUI()

        // Reinitialize the adapter and reload data
        val dailyCaloriesAdapter = DailyCaloriesAdapter(this, clickDailyCalories())
        caloriesFoodList.adapter = dailyCaloriesAdapter

        // Load data
        val dailyCaloriesList = DailyCalories().fetchAll(this).ifEmpty { listOf(DailyCalories()) }
        dailyCaloriesAdapter.loadData(dailyCaloriesList)
    }

    private fun clickDailyCalories() : OnItemClickListener {
        return object : OnItemClickListener {
            override fun onItemClick(dailyCalories: DailyCalories) {
                val intent = Intent(this@dailyCalories, formDailyCalories::class.java)
                intent.putExtra("dailyCaloriesObject", JSON.toJson(dailyCalories))
                startActivity(intent)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(dailyCalories: DailyCalories)
    }

    private fun callFormDailyCalories() {
        try {
            startActivity(Intent(this, formDailyCalories::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de calorias diárias: $e")
        }
    }

    private fun callFoodForm() {
        try {
            startActivity(Intent(this, formFood::class.java))
        } catch (e: Exception) {
            println("Erro ao chamar a tela de alimentos: $e")
        }
    }

    private fun loadAndUpdateMacroUI() {
        Macro().loadAndUpdateMacroUI(
            context = this,
            caloriesProgressBar = caloriesProgressBar,
            carbsProgressBar = carbsProgressBar,
            fatsProgressBar = fatsProgressBar,
            proteinsProgressBar = proteinsProgressBar,
            dietaryFiberProgressBar = dietaryFiberProgressBar,
            caloriesLabel = caloriesLabel,
            carbsLabel = carbsLabel,
            fatsLabel = lipidsLabel,
            proteinsLabel = proteinsLabel,
            dietaryFiberLabel = dietaryFiberLabel,
            miniVersion = false
        )
    }

}