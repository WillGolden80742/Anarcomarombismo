package com.example.anarcomarombismo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class playExercise : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var exerciseNameEditText: EditText
    private lateinit var muscleGroupTextView: TextView
    private lateinit var repetitionsEditText: EditText
    private lateinit var setsEditText: EditText
    private lateinit var loadEditText: EditText
    private lateinit var restEditText: EditText
    private lateinit var cadenceEditText: EditText
    private lateinit var addExerciseButton: Button
    private lateinit var removeExerciseButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_exercise)
        instantiateFields()
    }

    private fun instantiateFields() {
        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        embedVideo("")
        webView.setBackgroundColor(0x00000000)
        exerciseNameEditText = findViewById(R.id.editPlayTextExerciseName)
        muscleGroupTextView = findViewById(R.id.textViewPlayMuscleGroup)
        repetitionsEditText = findViewById(R.id.editPlayTextRepetitions)
        setsEditText = findViewById(R.id.editPlayTextSets)
        loadEditText = findViewById(R.id.editPlayTextLoad)
        restEditText = findViewById(R.id.editPlayTextRest)
        cadenceEditText = findViewById(R.id.editPlayTextCadence)
        addExerciseButton = findViewById(R.id.addPlayExerciseFormButton)
        removeExerciseButton = findViewById(R.id.removePlayExerciseFormButton)
    }

    private fun embedVideo(formattedLink: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (formattedLink.isNotEmpty()) {
                webView.loadUrl(formattedLink)
            } else {
                val text = withContext(Dispatchers.IO) {
                    val inputStream = resources.openRawResource(R.raw.vector_banner)
                    inputStream.bufferedReader().use { it.readText() }
                }
                webView.loadUrl("data:image/svg+xml;base64," + text)
            }
        }
    }

}
