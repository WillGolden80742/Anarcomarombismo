package com.example.anarcomarombismo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.JSON
import com.example.anarcomarombismo.Controller.Tree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class playExercise : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var exerciseNameEditText: EditText
    private lateinit var spinnerMuscleGroup: Spinner
    private lateinit var repetitionsEditText: EditText
    private lateinit var setsEditText: EditText
    private lateinit var loadEditText: EditText
    private lateinit var restEditText: EditText
    private lateinit var cadenceEditText: EditText
    private lateinit var addExerciseButton: CheckBox
    private lateinit var saveExerciseFormButton: Button
    private var trainingID: Long = 0
    private var exerciseID: Long = 0
    private lateinit var textVideoLink: String
    private var leafsNames: List<String> = listOf()
    private lateinit var leafsMap:Set<Tree>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_exercise)
        instantiateFields()
        loadExerciseIfExistInCache()
    }

    override fun onResume() {
        super.onResume()
        populateExerciseFields()
    }

    private fun instantiateFields() {
        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        embedVideo("")
        webView.setBackgroundColor(0x00000000)
        exerciseNameEditText = findViewById(R.id.editPlayTextExerciseName)
        spinnerMuscleGroup = findViewById(R.id.spinnerPlayMuscleGroup)
        repetitionsEditText = findViewById(R.id.editPlayTextRepetitions)
        setsEditText = findViewById(R.id.editPlayTextSets)
        loadEditText = findViewById(R.id.editPlayTextLoad)
        restEditText = findViewById(R.id.editPlayTextRest)
        cadenceEditText = findViewById(R.id.editPlayTextCadence)
        addExerciseButton = findViewById(R.id.addPlayExerciseFormButton)
        saveExerciseFormButton = findViewById(R.id.savePlayExerciseFormButton)
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
                webView.loadUrl("data:image/svg+xml;base64,$text")
            }
        }
    }

    private fun loadExerciseIfExistInCache(trainingID: Long = 0, exerciseID: Long = 0): Exercise {
        println("ID do exercício: $exerciseID")
        val cache = Cache()
        val jsonUtil = JSON()
        loadSpinner()

        val exercise = Exercise(trainingID = trainingID, exerciseID = exerciseID)

        CoroutineScope(Dispatchers.Main).launch {
            if (exerciseID > 0) {
                val exerciseArray = withContext(Dispatchers.IO) {
                    jsonUtil.fromJson(cache.getCache(this@playExercise, "Exercicios_$trainingID"), Array<Exercise>::class.java)
                }

                for (ex in exerciseArray) {
                    if (ex.exerciseID == exerciseID) {
                        // Preenchendo manualmente os atributos
                        exercise.trainingID = trainingID
                        exercise.exerciseID = exerciseID
                        exercise.LinkVideo = Exercise().generateYouTubeEmbedLink(ex.LinkVideo)
                        exercise.name = ex.name
                        exercise.muscle = ex.muscle
                        exercise.sets = ex.sets
                        exercise.repetitions = ex.repetitions
                        exercise.load = ex.load
                        exercise.rest = ex.rest
                        exercise.cadence = ex.cadence
                        embedVideo(exercise.LinkVideo)
                        addExerciseButton.text = getString(R.string.exercise_finished)
                        break
                    }
                }
            }
        }

        return exercise
    }
    private fun populateExerciseFields() {
        trainingID = intent.getLongExtra("trainingID", 0)
        exerciseID = intent.getLongExtra("exerciseID", 0)
        val exercise = loadExerciseIfExistInCache(trainingID, exerciseID)
        exerciseNameEditText.setText(exercise.name)
        val value = leafsNames.size
        for (i in 0 until value) {
            if (spinnerMuscleGroup.getItemAtPosition(i).toString() == exercise.muscle) {
                spinnerMuscleGroup.setSelection(i)
                break
            }
        }
        setsEditText.setText(exercise.sets.toString())
        repetitionsEditText.setText(exercise.repetitions)
        loadEditText.setText(exercise.load.toString())
        restEditText.setText(exercise.rest.toString())
        cadenceEditText.setText(exercise.cadence)
    }

    private fun loadSpinner() {
        try {
            leafsMap = Tree().dumpAndLoadMuscles(this)
            leafsNames = leafsMap.map { getString(it.obj as Int) }
            leafsNames = leafsNames.sorted()
            leafsNames = listOf(getString(R.string.select_muscle)) + leafsNames
            ArrayAdapter(this, android.R.layout.simple_spinner_item, leafsNames).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMuscleGroup.adapter = it
            }
            spinnerMuscleGroup.setSelection(0)
        } catch (e: Exception) {
            println("Erro ao carregar spinner de músculos: " + e.message)
        }
    }

}
