import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JSON {

    // Método para desserializar um JSON para um objeto específico
    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método fromJson do Gson para converter o JSON para o objeto desejado
        return gson.fromJson(json, classOfT)
    }

    // Método para desserializar um JSON para uma lista de objetos de um tipo genérico
    inline fun <reified T> fromJsonArray(json: String): List<T> {
        // Cria um TypeToken para representar uma lista do tipo genérico T
        val listType = object : TypeToken<List<T>>() {}.type
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método fromJson do Gson para converter o JSON para a lista de objetos do tipo T
        return gson.fromJson(json, listType)
    }

    // Método para serializar um objeto para JSON
    fun toJson(obj: Any): String {
        // Cria uma instância do Gson
        val gson = Gson()
        // Usa o método toJson do Gson para converter o objeto para JSON
        return gson.toJson(obj)
    }

    // Método para pesquisar um objeto JSON em um array pelo seu atributo e valor
    inline fun <reified T> searchJsonInArray(jsonArray: List<T>, attribute: String, value: String): List<T> {
        return jsonArray.filter {
            val json = Gson().toJson(it)
            json.contains("\"$attribute\":\"*$value*\"", ignoreCase = true)
        }
    }
}
