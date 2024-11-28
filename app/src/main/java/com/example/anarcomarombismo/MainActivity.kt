package com.example.anarcomarombismo

import MainAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.Training
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var launcher: ActivityResultLauncher<Array<String>>
    private lateinit var importTrainings: FloatingActionButton
    private lateinit var mainListView: ListView
    data class MainAdapterItem(
        val title: String,
        val subtitle: String,
        val iconResourceId: Int,
        val destinationActivity: Class<*>? = null
    )

    private fun initializeUIComponents() {
        importTrainings = findViewById(R.id.importTrainings)
        mainListView = findViewById(R.id.trainingList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeUIComponents()
        // Import/Export setup remains the same
        launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                Training().handleImportResult(it, this)
            } ?: showToastMessage(this, false, R.string.error_no_file_selected, R.string.error_no_file_selected)
        }
        importTrainings.setOnClickListener {
            launcher.launch(arrayOf("application/octet-stream"))
        }
    }


    override fun onResume() {
        super.onResume()
        handleIncomingFile(intent)
        val adapter = MainAdapter(this, buildMainItems())
        mainListView.adapter = adapter
    }

    private fun buildMainItems(): List<MainAdapterItem> {
        return listOf(
            MainAdapterItem(
                title = getString(R.string.trainings_label),
                subtitle = getString(R.string.manage_your_workouts),
                iconResourceId = R.drawable.muscle_icon,
                destinationActivity = trainings::class.java
            ),
            MainAdapterItem(
                title = getString(R.string.daily_calories),
                subtitle = getString(R.string.track_your_diet),
                iconResourceId = R.drawable.ic_fluent_food_24_regular,
                destinationActivity = dailyCalories::class.java
            )
        )
    }
    private fun handleIncomingFile(intent: Intent?) {
        intent?.let { safeIntent ->
            when (safeIntent.action) {
                Intent.ACTION_VIEW -> {
                    val uri = safeIntent.data
                    if (uri != null) {
                        Training.import(this,uri)
                        safeIntent.action = null
                    }
                }
            }
        }
    }

    private fun showToastMessage(context: Context, isUpdate: Boolean, addMessageId: Int, updateMessageId: Int) {
        val messageResId = if (isUpdate) updateMessageId else addMessageId
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}