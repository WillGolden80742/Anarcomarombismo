package com.example.anarcomarombismo.Forms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Tree
import com.example.anarcomarombismo.Controller.Util.ExerciseHandler
import com.example.anarcomarombismo.Controller.Util.WebHandler
import com.example.anarcomarombismo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class formExercise : AppCompatActivity() {
    // Views
    private lateinit var webView: WebView
    private lateinit var textViewVideoLink: TextView
    private lateinit var editTextVideoLink: EditText
    private lateinit var editTextExerciseName: EditText
    private lateinit var spinnerMuscleGroup: Spinner
    private lateinit var editTextSets: EditText
    private lateinit var editTextRepetitions: EditText
    private lateinit var editTextLoad: EditText
    private lateinit var editTextRest: EditText
    private lateinit var editTextCadence: EditText
    private lateinit var addExerciseButton: Button
    private lateinit var removeExerciseButton: Button
    private lateinit var checkExerciseFormButton: FloatingActionButton
    private lateinit var editExerciseFormButton: FloatingActionButton
    private lateinit var visualizeExerciseFormButton: LinearLayout
    // Data
    private var trainingID: Long = 0
    private var exerciseID: Long = 0
    private var exerciseDate: String = ""
    private var action: String = ""
    private lateinit var leafsMap: Set<Tree>
    private var leafsNames: List<String> = listOf()
    private var textVideoLink: String = ""
    private var currentExercise: Exercise? = null
    // Constants
    private val DOUBLE_CLICK_TIME_DELTA: Long = 300
    private var lastClickTime: Long = 0
    private fun initializeUIComponents() {
        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        WebHandler.embedVideoForm(this,webView,"")
        webView.setBackgroundColor(0x00000000)
        textViewVideoLink = findViewById(R.id.textViewVideoLink)
        editTextVideoLink = findViewById(R.id.editTextVideoLink)
        editTextExerciseName = findViewById(R.id.editTextExerciseName)
        spinnerMuscleGroup = findViewById(R.id.spinnerMuscleGroup)
        editTextSets = findViewById(R.id.editTextSets)
        editTextRepetitions = findViewById(R.id.editTextRepetitions)
        editTextLoad = findViewById(R.id.editTextLoad)
        editTextRest = findViewById(R.id.editTextRest) // Inicialização do novo campo para repouso
        editTextCadence = findViewById(R.id.editTextCadence) // Inicialização do novo campo para cadência
        addExerciseButton = findViewById(R.id.addExerciseFormButton)
        removeExerciseButton = findViewById(R.id.removeExerciseFormButton)
        visualizeExerciseFormButton = findViewById(R.id.visualizeExerciseFormButton)
        checkExerciseFormButton = findViewById(R.id.checkExerciseFormButton)
        editExerciseFormButton = findViewById(R.id.editExerciseFormButton)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_exercise)
        initializeUIComponents()
        trainingID = intent.getLongExtra("trainingID", 0)
        exerciseID = intent.getLongExtra("exerciseID", 0)
        exerciseDate = intent.getStringExtra("exerciseDate") ?: ""
        action = intent.getStringExtra("action") ?: ""
        when (action) {
            "edit" -> {
                enableButtons(true)
            }
            "play" -> {
                enableButtons(false)
            }
        }

        addExerciseButton.setOnClickListener {
            saveExercise()
        }

        removeExerciseButton.setOnClickListener {
            val clickTime = System.currentTimeMillis()
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                removeExercise()
            } else {
                Toast.makeText(this,
                    this.getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
            }
            lastClickTime = clickTime
        }

        editTextRepetitions.addTextChangedListener {
            ExerciseHandler.formatRepetitionsAndCountSets(editTextSets, editTextRepetitions)
        }

        // listener change editTextVideoLink focus lost
        editTextVideoLink.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    val formattedLink = WebHandler.generateYouTubeEmbedLink(editTextVideoLink.text.toString())
                    editTextVideoLink.setText(formattedLink)
                    WebHandler.embedVideoForm(this,webView,formattedLink)
                } catch (e: Exception) {
                    editTextVideoLink.setText("")
                    println("Erro ao formatar o link do vídeo: " + e.message)
                }
            }
        }

        editExerciseFormButton.setOnClickListener {
            enableButtons(true)
        }

        checkExerciseFormButton.setOnClickListener {
            checkExercise(currentExercise!!)
        }

    }
    override fun onResume() {
        super.onResume()
        loadExerciseIfExistInCache()
    }

    private fun isCheckExercise(exercise: Exercise): Boolean {
        return DailyExercises(this).isExerciseDone(exerciseDate,exercise)
    }
    private fun checkExercise(exercise: Exercise) {
        val dailyExercices = DailyExercises(this)
        if (isCheckExercise(exercise)) {
            checkExerciseFormButton.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
            dailyExercices.unmarkExercise(exerciseDate,currentExercise!!,editTextSets.text.toString().toInt())
        } else {
            checkExerciseFormButton.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
            dailyExercices.markExerciseAsDone(exerciseDate,currentExercise!!,editTextSets.text.toString().toInt())
            Toast.makeText(this, "${editTextExerciseName.text} ${this.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableButtons(enable: Boolean) {
        addExerciseButton.isEnabled = enable
        removeExerciseButton.isEnabled = enable
        editTextVideoLink.isEnabled = enable
        editTextExerciseName.isEnabled = enable
        spinnerMuscleGroup.isEnabled = enable
        editTextSets.isEnabled = enable
        editTextRepetitions.isEnabled = enable
        editTextLoad.isEnabled = enable
        editTextRest.isEnabled = enable
        editTextCadence.isEnabled = enable
        removeExerciseButton.isVisible = enable
        addExerciseButton.isVisible = enable
        editTextVideoLink.isVisible = enable
        textViewVideoLink.isVisible = enable
        visualizeExerciseFormButton.isVisible = !enable
    }

    private fun loadExerciseIfExistInCache() {
        CoroutineScope(Dispatchers.Main).launch {
            loadSpinner()
            val loadedExercise = Exercise.build(trainingID).fetchById(this@formExercise, exerciseID)
            if (loadedExercise != null) {
                currentExercise = loadedExercise
                setUIFromExercise(currentExercise!!)
            } else {
                visualizeExerciseFormButton.isVisible = false
                removeExerciseButton.isVisible = false
            }
        }
    }

    private fun setUIFromExercise(exercise: Exercise) {
        val formattedLink = WebHandler.generateYouTubeEmbedLink(exercise.linkVideo)
        textVideoLink = formattedLink
        editTextVideoLink.setText(formattedLink)
        WebHandler.embedVideoForm(this,webView,formattedLink)
        editTextExerciseName.setText(exercise.name)

        val value = leafsNames.size
        for (i in 0 until value) {
            if (spinnerMuscleGroup.getItemAtPosition(i).toString() == exercise.muscle) {
                spinnerMuscleGroup.setSelection(i)
                break
            }
        }

        editTextSets.setText(exercise.sets.toString())
        editTextRepetitions.setText(exercise.repetitions)
        editTextLoad.setText(exercise.load.toString())
        editTextRest.setText(exercise.rest.toString())
        editTextCadence.setText(exercise.cadence)
        addExerciseButton.text = getString(R.string.update_exercise)

        if (isCheckExercise(exercise)) {
            checkExerciseFormButton.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        } else {
            checkExerciseFormButton.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        }
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


    private fun saveExercise() {
        if (!areFieldsValid()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }
        if(buildExercise().save(this)) {
            finish()
        }
    }

    private fun buildExercise(): Exercise {
        val exerciseHint = getString(R.string.exercise_hint)
        val defaultCadence = getString(R.string.default_cadence)
        val defaultRest = getString(R.string.default_rest).toInt()
        val defaultReps = getString(R.string.default_reps)
        val defaultSets = getString(R.string.default_sets).toInt()

        return Exercise.build(
            trainingID = trainingID,
            linkVideo = editTextVideoLink.text.toString(),
            exerciseID = exerciseID,
            name = editTextExerciseName.text.toString().takeIf { it.isNotEmpty() } ?: exerciseHint,
            muscle = spinnerMuscleGroup.selectedItem.toString(),
            sets = editTextSets.text.toString().toIntOrNull() ?: defaultSets,
            repetitions = editTextRepetitions.text.toString().takeIf { it.isNotEmpty() } ?: defaultReps,
            load = editTextLoad.text.toString().toDoubleOrNull() ?: 0.0,
            rest = editTextRest.text.toString().toIntOrNull() ?: defaultRest,
            cadence = editTextCadence.text.toString().takeIf { it.isNotEmpty() } ?: defaultCadence
        )
    }

    private fun areFieldsValid(): Boolean {
        return spinnerMuscleGroup.selectedItemPosition != 0 &&
                editTextExerciseName.text.toString().isNotEmpty() &&
                editTextSets.text.toString().isNotEmpty() &&
                editTextRepetitions.text.toString().isNotEmpty() &&
                editTextLoad.text.toString().isNotEmpty()
    }

    private fun removeExercise() {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            val exercise = buildExercise()
            exercise.remove(this)
            finish()
        } else {
            Toast.makeText(this,
                this.getString(R.string.double_click_fast_for_exclusion), Toast.LENGTH_SHORT).show()
        }
        lastClickTime = clickTime
    }

}
