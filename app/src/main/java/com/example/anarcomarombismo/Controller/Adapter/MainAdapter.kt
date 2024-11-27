import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.anarcomarombismo.R
import com.example.anarcomarombismo.dailyCalories
import com.example.anarcomarombismo.trainings
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainAdapter(
    context: Context,
    items: List<String>,
    private val onItemClickListener: ((String) -> Unit)? = null
) : ArrayAdapter<String>(context, R.layout.main_list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.main_list_item, parent, false)

        val titleTextView: TextView = view.findViewById(R.id.mainTitleTextViewItem)
        val subtitleTextView: TextView = view.findViewById(R.id.trainingTextViewItem)
        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)

        val item = getItem(position)
        titleTextView.text = item

        // Set subtitles or additional info if needed
        subtitleTextView.text = when (item) {
            "Treinos" -> "Gerencie seus treinos"
            "Calorias Diárias" -> "Acompanhe sua dieta"
            else -> ""
        }

        // Definindo o ícone do FloatingActionButton baseado no item
        when (item) {
            "Treinos" -> {
                floatingActionButton.setImageResource(R.drawable.muscle_icon)
            }
            "Calorias Diárias" -> {
                floatingActionButton.setImageResource(R.drawable.ic_fluent_food_24_regular)
            }
            else -> {
                floatingActionButton.setImageResource(R.drawable.ic_fluent_play_24_regular) // Ícone padrão
            }
        }

        view.setOnClickListener {
            when (item) {
                "Treinos" -> {
                    val intent = Intent(context, trainings::class.java)
                    context.startActivity(intent)
                }
                "Calorias Diárias" -> {
                    val intent = Intent(context, dailyCalories::class.java)
                    context.startActivity(intent)
                }
            }
            item?.let { it1 -> onItemClickListener?.invoke(it1) }
        }

        return view
    }
}