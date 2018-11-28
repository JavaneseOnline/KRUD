package online.javanese.krud.kwery.kweryEntityMapping

import com.github.andrewoma.kwery.mapper.Table
import io.ktor.util.StringValues

/**
 * Helps creating Kwery entity from a map.
 * Useful for mapping HTTP request variables onto an object.
 */
class MapToKweryEntityMapper<out T : Any, ID>(
        private val table: Table<T, ID>,
        fallback: Map<String, *> = emptyMap<String, Nothing>(),
        private val transformName: (String) -> String = { it }
) : (StringValues) -> T {

    // TODO validation: return an instance of a sealed class ( success | error )

    private val valueFactory = ValueFactory(table, fallback)

    override fun invoke(map: StringValues): T =
            table.create(valueFactory.from(RenamedStringValues(map, transformName)))

}

private class RenamedStringValues(
        private val map: StringValues,
        private val transformName: (String) -> String
) : StringValues {
    override val caseInsensitiveName: Boolean get() = map.caseInsensitiveName
    override fun get(name: String): String? = map[transformName(name)]
    override fun getAll(name: String): List<String>? = map.getAll(transformName(name))
    override fun names(): Set<String> = error("")
    override fun entries(): Set<Map.Entry<String, List<String>>> = error("")
    override fun contains(name: String): Boolean = map.contains(transformName(name))
    override fun contains(name: String, value: String): Boolean = map.contains(transformName(name), value)
    override fun forEach(body: (String, List<String>) -> Unit) = error("")
    override fun isEmpty(): Boolean = map.isEmpty()
}
