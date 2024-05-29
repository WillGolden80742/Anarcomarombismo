package com.example.anarcomarombismo

import com.example.anarcomarombismo.Controller.JSON
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class formExercise : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var editTextVideoLink: EditText
    private lateinit var editTextExerciseName: EditText
    private lateinit var editTextSets: EditText
    private lateinit var editTextRepetitions: EditText
    private lateinit var editTextLoad: EditText
    private lateinit var editTextRest: EditText // Novo campo para repouso
    private lateinit var editTextCadence: EditText // Novo campo para cadência
    private lateinit var addExerciseButton: Button
    private lateinit var removeExerciseButton: Button
    private var trainingID: Long = 0
    private var exerciseID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_exercise)
        instantiateFields()
        trainingID = intent.getLongExtra("trainingID", 0)
        exerciseID = intent.getLongExtra("exerciseID", 0)
        println("ID do exercio: $exerciseID")
        loadExerciseIfExistInCache()
        addExerciseButton.setOnClickListener {
            saveExercise()
        }
        removeExerciseButton.setOnClickListener {
            removeExercise()
        }
        // listener change text editTextRepetitions
        editTextRepetitions.addTextChangedListener {
            formatRepetitionsAndCountSets(it)
        }
        // listener change editTextVideoLink focus lost
        editTextVideoLink.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    val formattedLink = generateYouTubeEmbedLink(editTextVideoLink.text.toString())
                    editTextVideoLink.setText(formattedLink)
                    embedVideo(formattedLink)
                } catch (e: Exception) {
                    editTextVideoLink.setText("")
                    System.out.println("Erro ao formatar o link do vídeo: " + e.message)
                }
            }
        }
    }

    fun formatRepetitionsAndCountSets(it: CharSequence?) {
        CoroutineScope(Dispatchers.Main).launch {
            val text = it.toString()
            val newText = text.replace(Regex("[^0-9Xx*,]"), "")
            if (text.contains("X") || text.contains("x") || text.contains("*")) {
                handleXFormat(text)
            } else if (text.contains(",")) {
                handleCommaFormat(text)
            } else if (newText != text) {
                editTextRepetitions.setText(newText)
            }
        }
    }

    private fun handleXFormat(text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val newText = async {
                text.replace(Regex("[^0-9Xx*]|X{2,}|x{2,}|\\*{2,}"), "")
            }
            val processedText = async {
                val xCount = newText.await().count { it == 'X' || it == 'x' || it == '*' }
                if (xCount > 1) {
                    newText.await().dropLast(1)
                } else {
                    newText.await()
                }
            }
            val numbers = async {
                processedText.await().split(Regex("[Xx*]")).filter { it.isNotEmpty() }
            }
            withContext(Dispatchers.Main) {
                try {
                    editTextSets.setText(numbers.await()[0])
                } catch (e: Exception) {
                    editTextSets.setText("1")
                }
                editTextRepetitions.setText("")
                if (processedText.await() != text) {
                    editTextRepetitions.setText(processedText.await())
                    editTextRepetitions.setSelection(processedText.await().length)
                }
            }
        }
    }

    private fun handleCommaFormat(text: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val newText = async {
                text.replace(Regex("[^0-9,]|,{2,}"), "")
            }
            val numbers = async {
                newText.await().split(",").filter { it.isNotEmpty() }
            }
            withContext(Dispatchers.Main) {
                editTextSets.setText(numbers.await().size.toString())
                if (newText.await() != text) {
                    editTextRepetitions.setText(newText.await())
                    editTextRepetitions.setSelection(newText.await().length)
                }
            }
        }
    }
    private fun instantiateFields() {
        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        embedVideo("")
        webView.setBackgroundColor(0x00000000)
        editTextVideoLink = findViewById(R.id.editTextVideoLink)
        editTextExerciseName = findViewById(R.id.editTextExerciseName)
        editTextSets = findViewById(R.id.editTextSets)
        editTextRepetitions = findViewById(R.id.editTextRepetitions)
        editTextLoad = findViewById(R.id.editTextLoad)
        editTextRest = findViewById(R.id.editTextRest) // Inicialização do novo campo para repouso
        editTextCadence = findViewById(R.id.editTextCadence) // Inicialização do novo campo para cadência
        addExerciseButton = findViewById(R.id.addExerciseFormButton)
        removeExerciseButton = findViewById(R.id.removeExerciseFormButton)
    }

    private fun loadExerciseIfExistInCache() {
        val cache = Cache()
        val jsonUtil = JSON()

        CoroutineScope(Dispatchers.Main).launch {
            if (exerciseID > 0) {
                val exerciseArray = withContext(Dispatchers.IO) {
                    jsonUtil.fromJson(cache.getCache(this@formExercise, "Exercicios_$trainingID"), Array<Exercise>::class.java)
                }

                for (exercise in exerciseArray) {
                    if (exercise.exerciseID == exerciseID) {
                        val formattedLink = generateYouTubeEmbedLink(exercise.LinkVideo)
                        editTextVideoLink.setText(formattedLink)
                        embedVideo(formattedLink)
                        editTextExerciseName.setText(exercise.name)
                        editTextSets.setText(exercise.sets.toString())
                        editTextRepetitions.setText(exercise.repetitions.toString())
                        editTextLoad.setText(exercise.load.toString())
                        editTextRest.setText(exercise.rest.toString())
                        editTextCadence.setText(exercise.cadence)
                        addExerciseButton.text = "Atualizar Exercício"
                    }
                }
            } else {
                removeExerciseButton.isVisible = false
            }
        }
    }

    fun embedVideo(formattedLink: String) {
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

    override fun onResume() {
        super.onResume()
        loadExerciseIfExistInCache()
    }

    private fun saveExercise() {
        val cache = Cache()
        val jsonUtil = JSON()
        val defaultCadence = getString(R.string.default_cadence)
        val defaultRest = getString(R.string.default_rest)
        val defaultReps = getString(R.string.default_reps)
        val defaultSets = getString(R.string.default_sets)
        val exerciseHint = getString(R.string.exercise_hint)

        // Verificar se todos os campos estão preenchidos
        if (editTextVideoLink.text.toString().isEmpty() || editTextExerciseName.text.toString().isEmpty() || editTextSets.text.toString().isEmpty() || editTextRepetitions.text.toString().isEmpty() || editTextLoad.text.toString().isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val exerciseArray = if (cache.hasCache(this, "Exercicios_$trainingID")) {
            jsonUtil.fromJson(cache.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }

        var exercise = Exercise(
            trainingID,
            editTextVideoLink.text.toString(),
            exerciseID,
            editTextExerciseName.text.toString().takeIf { it.isNotEmpty() } ?: exerciseHint,
            editTextSets.text.toString().toIntOrNull() ?: defaultSets.toInt(),
            editTextRepetitions.text.toString() ?: defaultReps,
            editTextLoad.text.toString().toDoubleOrNull() ?: 0.0,
            editTextRest.text.toString().toIntOrNull() ?: defaultRest.toInt(),
            editTextCadence.text.toString().takeIf { it.isNotEmpty() } ?: defaultCadence
        )

        val newExerciseArray = if (exerciseID > 0) {
            exerciseArray.map {
                if (it.exerciseID == exerciseID) {
                    exercise
                } else {
                    it
                }
            }.toTypedArray()
        } else {
            exercise.exerciseID = System.currentTimeMillis()
            exerciseArray.plus(exercise)
        }

        cache.setCache(this, "Exercicios_$trainingID", jsonUtil.toJson(newExerciseArray))
        if (exerciseID > 0) {
            Toast.makeText(this, getString(R.string.update_exercise_successful), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.save_exercise_successful), Toast.LENGTH_SHORT).show()
        }
        finish()
    }


    private fun removeExercise() {
        val cache = Cache()
        val jsonUtil = JSON()
        val exerciseArray = if (cache.hasCache(this, "Exercicios_$trainingID")) {
            jsonUtil.fromJson(cache.getCache(this, "Exercicios_$trainingID"), Array<Exercise>::class.java)
        } else {
            arrayOf()
        }
        val newExerciseArray = exerciseArray.filter {
            it.exerciseID != exerciseID
        }.toTypedArray()
        cache.setCache(this, "Exercicios_$trainingID", jsonUtil.toJson(newExerciseArray))
        Toast.makeText(this, getString(R.string.remove_exercise_successful), Toast.LENGTH_SHORT).show()
        finish()
    }

    fun generateYouTubeEmbedLink(text: String): String {
        var modifiedText = text.trim()

        // Return empty string if the input doesn't contain YouTube or youtu.be links
        if (!modifiedText.contains("youtu.be") && !modifiedText.contains("youtube")) {
            return ""
        }

        // Return the link if it's already in embed format
        if (modifiedText.contains("https://www.youtube.com/embed/")) {
            return modifiedText
        }

        // Remove unnecessary parameters
        modifiedText = modifiedText.replace(Regex("[&?](feature=youtu\\.be|si=.*)"), "")

        // Extract the time parameter if it exists
        val timeParameter = modifiedText.substringAfter("&t=", "").let {
            if (it.isNotEmpty()) "?t=$it" else ""
        }
        modifiedText = modifiedText.substringBefore("&t=")

        // Extract video ID based on different URL patterns
        val videoId = when {
            modifiedText.contains("/live/") -> modifiedText.substringAfter("/live/").substringBefore("?")
            modifiedText.contains("/shorts/") -> modifiedText.substringAfter("/shorts/").substringBefore("?")
            modifiedText.contains("youtube.com/") -> {
                if (modifiedText.contains("watch?v=")) {
                    modifiedText.substringAfter("watch?v=").substringBefore("&")
                } else ""
            }
            modifiedText.contains("youtu.be/") -> modifiedText.substringAfter("youtu.be/").substringBefore("?")
            else -> ""
        }

        // Return empty string if no valid video ID is found
        if (videoId.isEmpty()) {
            return ""
        }

        // Generate the embed link
        val embedLink = "https://www.youtube.com/embed/$videoId$timeParameter"
        return embedLink
    }


}
