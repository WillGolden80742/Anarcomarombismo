import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.anarcomarombismo.Controller.Macro
import com.example.anarcomarombismo.Controller.Util.Cache
import com.example.anarcomarombismo.MainActivity
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCalories
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainAdapter(
    context: Context,
    items: List<MainActivity.MainAdapterItem>,
    private val onItemClickListener: ((MainActivity.MainAdapterItem) -> Unit)? = null
) : ArrayAdapter<MainActivity.MainAdapterItem>(context, R.layout.main_list_item, items) {

    private val contextualKey = context.getString(R.string.dailycalories)
    private val cache = Cache()
    private lateinit var progressBarContainer: LinearLayout

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false)
        progressBarContainer = view.findViewById(R.id.progressBarContainer)
        val item = getItem(position)
        item?.let { mainItem ->
            val titleTextView: TextView = view.findViewById(R.id.mainTitleTextViewItem)
            val subtitleTextView: TextView = view.findViewById(R.id.trainingTextViewItem)
            val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)

            titleTextView.text = mainItem.title
            subtitleTextView.text = mainItem.subtitle
            floatingActionButton.setImageResource(mainItem.iconResourceId)

            view.setOnClickListener {
                mainItem.destinationActivity?.let { activity ->
                    val intent = Intent(context, activity)
                    context.startActivity(intent)
                }
                onItemClickListener?.invoke(mainItem)
            }
            progressBarContainer.isVisible = false
            if (mainItem.destinationActivity == dailyCalories::class.java && cache.hasCache(context, contextualKey)) {
                updateMacroUI(view)
            }
        }

        return view
    }

    private fun updateMacroUI(view: View) {
        CoroutineScope(Dispatchers.Main).launch {
            val caloriesProgressBar: ProgressBar = view.findViewById(R.id.caloriesProgressBar)
            val carbsProgressBar: ProgressBar = view.findViewById(R.id.carbsProgressBar)
            val fatsProgressBar: ProgressBar = view.findViewById(R.id.fatsProgressBar)
            val proteinsProgressBar: ProgressBar = view.findViewById(R.id.proteinsProgressBar)
            val dietaryFiberProgressBar: ProgressBar = view.findViewById(R.id.dietaryFiberProgressBar)
            val caloriesLabel: TextView = view.findViewById(R.id.caloriesLabel)
            val carbsLabel: TextView = view.findViewById(R.id.carbsLabel)
            val lipidsLabel: TextView = view.findViewById(R.id.lipidsLabel)
            val proteinsLabel: TextView = view.findViewById(R.id.proteinsLabel)
            val dietaryFiberLabel: TextView = view.findViewById(R.id.dietaryFiberLabel)

            withContext(Dispatchers.Main) {
                Macro().loadAndUpdateMacroUI(
                    context = context,
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
                progressBarContainer.isVisible = true
            }
        }
    }


}
