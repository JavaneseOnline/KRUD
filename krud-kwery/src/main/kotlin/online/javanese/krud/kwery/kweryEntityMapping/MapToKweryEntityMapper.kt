package online.javanese.krud.kwery.kweryEntityMapping

import com.github.andrewoma.kwery.mapper.Table

/**
 * Helps creating Kwery entity from a map.
 * Useful for mapping HTTP request variables onto an object.
 */
class MapToKweryEntityMapper<out T : Any, ID>(
        private val table: Table<T, ID>,
        fallbackSource: ValueFactory.Source<*> = ValueFactory.EmptySource,
        private val nameTransform: (String) -> String = { it }
) : (Map<String, String>) -> T {

    // TODO validation: return an instance of a sealed class ( success | error )

    private val valueFactory = ValueFactory(table, fallbackSource)

    override fun invoke(map: Map<String, String>): T =
            table.create(valueFactory.from(MapAsSource(map, nameTransform)))

}
