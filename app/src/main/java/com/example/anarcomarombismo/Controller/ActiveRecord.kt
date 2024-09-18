import android.content.Context
import android.widget.Toast
import com.example.anarcomarombismo.Controller.Cache
import com.example.anarcomarombismo.Controller.JSON

open class ActiveRecord<T : Any>(private val clazz: Class<T>) {

    private val cache = Cache()
    private val jsonUtil = JSON()

    // Método para salvar ou atualizar o objeto
    fun save(
        context: Context,
        cacheKey: String,
        obj: T,
        successMessage: String = "Salvo com sucesso!",
        failureMessage: String = "Erro ao salvar objeto."
    ): Boolean {
        return handleOperation(
            context,
            cacheKey,
            obj,
            successMessage,
            failureMessage,
            onListUpdate = { list, obj ->
                val index = list.indexOfFirst { it == obj }
                if (index != -1) list[index] = obj else list.add(obj)
            }
        )
    }

    // Método para remover um objeto
    fun remove(
        context: Context,
        cacheKey: String,
        obj: T,
        successMessage: String = "Removido com sucesso!",
        notFoundMessage: String = "Objeto não encontrado.",
        failureMessage: String = "Erro ao remover objeto."
    ): Boolean {
        return handleOperation(
            context,
            cacheKey,
            obj,
            successMessage,
            failureMessage,
            onListUpdate = { list, obj ->
                if (!list.remove(obj)) throw ObjectNotFoundException(notFoundMessage)
            }
        )
    }

    // Método genérico para carregar a lista, manipular e salvar no cache
    private fun handleOperation(
        context: Context,
        cacheKey: String,
        obj: T,
        successMessage: String,
        failureMessage: String,
        onListUpdate: (MutableList<T>, T) -> Unit
    ): Boolean {
        return try {
            val list = loadList(context, cacheKey).toMutableList()
            onListUpdate(list, obj)
            cache.setCache(context, cacheKey, jsonUtil.toJson(list))
            showToast(context, successMessage)
            true
        } catch (e: ObjectNotFoundException) {
            showToast(context, e.message ?: failureMessage)
            false
        } catch (e: Exception) {
            showToast(context, failureMessage)
            false
        }
    }

    // Método para carregar uma lista de objetos do cache
    private fun loadList(context: Context, cacheKey: String): List<T> {
        val cachedData = cache.getCache(context, cacheKey)
        return if (cache.hasCache(context, cacheKey) && cachedData != null) {
            jsonUtil.fromJson(cachedData, clazz) as List<T>
        } else {
            emptyList()
        }
    }

    // Método para exibir mensagens de Toast
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Exceção customizada para tratar objetos não encontrados
    class ObjectNotFoundException(message: String) : Exception(message)
}
