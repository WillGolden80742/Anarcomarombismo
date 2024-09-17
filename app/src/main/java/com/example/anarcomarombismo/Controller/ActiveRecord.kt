import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.JSON

open class ActiveRecord<T : Any>(private val clazz: Class<T>) {
    private val cache = Cache()
    private val jsonUtil = JSON()

    fun save(context: Context, cacheKey: String, obj: T, successMessage: String = "Salvo com sucesso!",failureMessage: String = "Erro ao salvar objeto."): Boolean {
        try {
            val list = loadList(context, cacheKey).toMutableList()
            val index = list.indexOfFirst { it == obj }

            if (index != -1) {
                list[index] = obj
            } else {
                list.add(obj)
            }

            cache.setCache(context, cacheKey, jsonUtil.toJson(list))
            showToast(context, successMessage)
            return true
        } catch (e: Exception) {
            showToast(context, failureMessage)
            return false
        }
    }

    fun remove(
        context: Context,
        cacheKey: String,
        obj: T,
        successMessage: String = "Removido com sucesso!",
        notFoundMessage: String = "Objeto não encontrado.",
        failureMessage: String = "Erro ao remover objeto."
    ): Boolean {
        try {
            val list = loadList(context, cacheKey).toMutableList()
            val removed = list.remove(obj)

            if (removed) {
                cache.setCache(context, cacheKey, jsonUtil.toJson(list))
                showToast(context, successMessage)
                return true
            } else {
                showToast(context, notFoundMessage)
                return false
            }
        } catch (e: Exception) {
            showToast(context, failureMessage)
            return false
        }
    }

    private fun loadList(context: Context, cacheKey: String): List<T> {
        return if (cache.hasCache(context, cacheKey)) {
            val cachedData = cache.getCache(context, cacheKey)
            jsonUtil.fromJsonArray(cachedData, clazz)
        } else {
            emptyList()
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
