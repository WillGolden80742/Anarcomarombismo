import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.MainActivity
import com.example.anarcomarombismo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainAdapter(
    context: Context,
    items: List<MainActivity.MainAdapterItem>,
    private val onItemClickListener: ((MainActivity.MainAdapterItem) -> Unit)? = null
) : ArrayAdapter<MainActivity.MainAdapterItem>(context, R.layout.main_list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false)

        val titleTextView: TextView = view.findViewById(R.id.mainTitleTextViewItem)
        val subtitleTextView: TextView = view.findViewById(R.id.trainingTextViewItem)
        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)

        val item = getItem(position)
        item?.let { mainItem ->
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
        }

        return view
    }
}