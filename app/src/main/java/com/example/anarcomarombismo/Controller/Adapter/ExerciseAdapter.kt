package com.example.anarcomarombismo.Controller.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.anarcomarombismo.Controller.DailyExercises
import com.example.anarcomarombismo.Controller.Exercise
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.Controller.Util.WebHandler
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.Forms.formExercise
import com.example.anarcomarombismo.completeWorkoutActivity
import com.example.anarcomarombismo.stopWatch
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAdapter(
    private val context: Context,
    private val exerciseList: List<Exercise>,
    private var date: String,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    companion object {
        private var isLongPressActive = false
        private val cache = Cache()
        private const val positionKey = "exercise_position"
        fun setBeginTime(context: Context, trainingId: Long) {
            val cache = Cache()
            val fileName = "training_begin_time_$trainingId"
            cache.setCache(context, fileName, System.currentTimeMillis())
        }

        private fun getBeginTime(context: Context, trainingId: Long): Long {
            val cache = Cache()
            val fileName = "training_begin_time_$trainingId"
            return if (cache.hasCache(context, fileName)) {
                cache.getCache(context, fileName, Long::class.java)
            } else {
                0L
            }
        }

        fun setEndTime(context: Context, trainingId: Long) {
            val cache = Cache()
            val fileName = "training_end_time_$trainingId"
            cache.setCache(context, fileName, System.currentTimeMillis())
        }

        private fun getEndTime(context: Context, trainingId: Long): Long {
            val cache = Cache()
            val fileName = "training_end_time_$trainingId"
            return if (cache.hasCache(context, fileName)) {
                cache.getCache(context, fileName, Long::class.java)
            } else {
                0L
            }
        }

        fun getTimeStamp(context: Context, trainingId: Long): Long {
            val beginTime = getBeginTime(context, trainingId)
            val endTime = getEndTime(context, trainingId)

            return if (beginTime > 0 && endTime > 0) {
                endTime - beginTime
            } else {
                0L
            }
        }

        fun getItemPositionIndex(context: Context,trainingId: Long): Int {
            val positionKey = "${positionKey}_$trainingId"
            if (cache.hasCache(context,positionKey)) {
                return cache.getCache(context,positionKey,Int::class.java)
            } else {
                cache.setCache(context,positionKey,0)
                return 0
            }
        }
        fun setItemPositionIndex(context: Context,trainingId: Long, index: Int) {
            val positionKey = "${positionKey}_$trainingId"
            cache.setCache(context,positionKey,index)
        }

    }

    interface OnExerciseCheckListener {
        fun onExerciseCheckChanged()
    }

    private val checkListener = context as? OnExerciseCheckListener

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val webView: WebView = itemView.findViewById(R.id.webView)
        val labelCheckBoxItem: TextView = itemView.findViewById(R.id.labelCheckBoxItem)
        val nameTextView: TextView = itemView.findViewById(R.id.titleTextViewItem)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewItem)
        val checkItem: FloatingActionButton = itemView.findViewById(R.id.checkBoxItem)
        val lookAtExercise: FloatingActionButton = itemView.findViewById(R.id.lookAtExercise)
        val floatingEditExerciseActionButton: FloatingActionButton = itemView.findViewById(R.id.floatingEditExerciseActionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.exercise_list_item, parent, false)
        return ExerciseViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentExercise = exerciseList[position]
        holder.nameTextView.text = currentExercise.name
        holder.descriptionTextView.text = currentExercise.toString(context)
        setupWebView(holder.webView, currentExercise)
        updateCheckItem(holder, currentExercise)
        updateDaysLabel(holder.labelCheckBoxItem, currentExercise)
        holder.floatingEditExerciseActionButton.setOnClickListener {
            callFormExercise("edit", currentExercise)
        }
        holder.checkItem.setOnClickListener {
            handleSetsCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
            checkListener?.onExerciseCheckChanged()
            val dailyExercises = DailyExercises(context)
            val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
            val sets = currentExercise.sets
            if (exerciseCount == sets) {
                Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
            }
        }
        holder.checkItem.setOnLongClickListener {
            isLongPressActive = true
            Toast.makeText(context, "${currentExercise.name} ${context.getString(R.string.finished)}", Toast.LENGTH_SHORT).show()
            true
        }
        holder.checkItem.setOnTouchListener { _, event ->
            when (event.action) {
                ACTION_UP -> {
                    if (isLongPressActive) {
                        checkSets(holder, currentExercise)
                        isLongPressActive = false
                    }
                }
                ACTION_CANCEL -> {
                    isLongPressActive = false
                }
            }
            checkListener?.onExerciseCheckChanged()
            false
        }
        holder.lookAtExercise.setOnClickListener {
            callFormExercise("play", currentExercise)
        }
    }

    private fun setupWebView(webView: WebView, currentExercise: Exercise) {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.setBackgroundColor(0x00000000)
        CoroutineScope(Dispatchers.Main).launch {
            configureWebView(webView, currentExercise)
        }
    }

    private fun configureWebView(webView: WebView, currentExercise: Exercise) {
        val embedLink = WebHandler.generateYouTubeEmbedLink(currentExercise.linkVideo)
        WebHandler.embedVideo(context, webView, embedLink)
        webView.setOnTouchListener { _, event ->
            if (event.action == ACTION_UP) {
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        simulateTouch(webView)
                    }
                }
                if (WebHandler.isNetworkAvailable(context)) {
                    webView.loadUrl(embedLink)
                    webView.setOnTouchListener(null)
                } else {
                    Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
                }
            }
            false
        }
    }

    private fun simulateTouch(webView: WebView) {
        val webViewWidth = webView.width
        val webViewHeight = webView.height
        val x = webViewWidth / 2f
        val y = webViewHeight / 2f
        val downEvent = MotionEvent.obtain(
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            MotionEvent.ACTION_DOWN,
            x,
            y,
            0
        )
        val upEvent = MotionEvent.obtain(
            System.currentTimeMillis(),
            System.currentTimeMillis() + 50,
            ACTION_UP,
            x,
            y,
            0
        )
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
    }

    private fun openStopWatchActivity(currentExerciseName: String, setsInfo: String) {
        val intent = Intent(context, stopWatch::class.java)
        intent.putExtra("exerciseName", currentExerciseName)
        intent.putExtra("setsInfo", setsInfo)
        context.startActivity(intent)
    }

    private fun checkSets(holder: ExerciseViewHolder, currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        val countDays = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        if (exerciseCount == 0 || countDays > 0) {
            repeat(sets) {
                handleSetsCheck(currentExercise, holder.labelCheckBoxItem, holder.checkItem)
            }
        } else {
            unmarkExerciseAsDone(dailyExercises, currentExercise, holder.checkItem)
            getExerciseStatusText(holder.labelCheckBoxItem, currentExercise)
        }
    }

    override fun getItemCount() = exerciseList.size

    private fun callFormExercise(action: String, exercise: Exercise) {
        val intent = Intent(context, formExercise::class.java).apply {
            putExtra("trainingID", exercise.trainingID)
            putExtra("exerciseID", exercise.exerciseID)
            putExtra("exerciseDate", date)
            putExtra("action", action)
        }
        context.startActivity(intent)
    }

    private fun getCurrentDate(): String {
        val currentDate = Date().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(currentDate)
    }

    private fun handleSetsCheck(currentExercise: Exercise, labelCheckBoxItem: TextView, checkItem: FloatingActionButton) {
        val dailyExercises = DailyExercises(context)
        val exerciseDone = dailyExercises.isExerciseDone(date, currentExercise)
        val exerciseDaysCount = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val sets = currentExercise.sets
        if (shouldCheckExercise(exerciseDaysCount, exerciseCount, sets)) {
            markSetsAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            toggleExerciseState(dailyExercises, exerciseDone, currentExercise, checkItem)
        }
        updateDaysLabel(labelCheckBoxItem, currentExercise)
    }


    private fun shouldCheckExercise(exerciseDaysCount: Int, setsCount: Int, repetitions: Int): Boolean {
        return exerciseDaysCount == 0 && setsCount < repetitions
    }

    private fun markSetsAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_on_24_filled)
        dailyExercises.markSetsAsDone(date, currentExercise)
        val trainingCompletionPercentage = dailyExercises.getTrainingCompletionPercentage(currentExercise.trainingID)
        val trainingId = currentExercise.trainingID
        if (dailyExercises.getCompletedSets(trainingId) == 1) {
            setBeginTime(context, trainingId)
        }
        if (trainingCompletionPercentage.toInt() == 100) {
            setEndTime(context, trainingId)
            val intent = Intent(context, completeWorkoutActivity::class.java)
            intent.putExtra("trainingId", trainingId)
            context.startActivity(intent)
        }
        val isLastExercise = trainingCompletionPercentage.toInt() == 100
        val exerciseCount = dailyExercises.getExerciseCount(currentExercise)
        val daysSinceLastExercise = dailyExercises.getDaysSinceLastExercise(currentExercise)
        val sets = currentExercise.sets
        val isLastSet = exerciseCount == sets
        if (isLastExercise && isLastSet) {
            setItemPositionIndex(context,currentExercise.trainingID, 0)
        }
        if (isLastSet) {
           scrollToNextExercise(currentExercise)
        }
        if (!isLongPressActive && daysSinceLastExercise == 0 && !isLastExercise) {
            val labelSets = DailyExercises(context).getExerciseStatusText(currentExercise)
            openStopWatchActivity(currentExercise.name,labelSets)
        }
    }

    private fun getFormattedDuration(context: Context, trainingId: Long): String {
        val elapsedMillis = getTimeStamp(context, trainingId)
        val hours = elapsedMillis / 3600000
        val minutes = (elapsedMillis % 3600000) / 60000
        val seconds = (elapsedMillis % 60000) / 1000
        val millis = (elapsedMillis % 1000) / 10 // Exibe os centésimos de segundo
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, millis)
    }

    private fun toggleExerciseState(dailyExercises: DailyExercises, exerciseDone: Boolean, currentExercise: Exercise, checkItem: FloatingActionButton) {
        if (exerciseDone) {
            unmarkExerciseAsDone(dailyExercises, currentExercise, checkItem)
        } else {
            markSetsAsDone(dailyExercises, currentExercise, checkItem)
        }
    }

    private fun unmarkExerciseAsDone(dailyExercises: DailyExercises, currentExercise: Exercise, checkItem: FloatingActionButton) {
        checkItem.setImageResource(R.drawable.ic_fluent_select_all_off_24_regular)
        dailyExercises.unmarkExercise(date, currentExercise)
    }

    private fun updateDaysLabel(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        getExerciseStatusText(labelCheckBoxItem, currentExercise)
    }

    private fun getExerciseStatusText(labelCheckBoxItem: TextView, currentExercise: Exercise) {
        labelCheckBoxItem.text =  DailyExercises(context).getExerciseStatusText(currentExercise)
    }

    private fun scrollToNextExercise(currentExercise: Exercise) {
        val dailyExercises = DailyExercises(context)
        val exerciseListSize = exerciseList.size
        var nextPosition = (exerciseList.indexOf(currentExercise) + 1) % exerciseListSize
        while (true) {
            val nextExercise = exerciseList[nextPosition]
            val countDays = dailyExercises.getDaysSinceLastExercise(nextExercise)
            val exerciseCount = dailyExercises.getExerciseCount(nextExercise)
            val sets = nextExercise.sets
            val isExerciseDone = dailyExercises.isExerciseDone(date, nextExercise)
            if (exerciseCount < sets || countDays > 0 || !isExerciseDone) {
                recyclerView.smoothScrollToPosition(nextPosition)
                break
            }
            nextPosition = (nextPosition + 1) % exerciseListSize
            if (nextPosition == exerciseList.indexOf(currentExercise)) break
        }
    }


    private fun updateCheckItem(holder: ExerciseViewHolder, currentExercise: Exercise) {
        val checked = DailyExercises(context).isExerciseDone(date, currentExercise)
        holder.checkItem.setImageResource(
            if (checked) R.drawable.ic_fluent_select_all_on_24_filled
            else R.drawable.ic_fluent_select_all_off_24_regular
        )
    }
}
