package online.javanese.krud.kwery.kweryEntityMapping

import com.github.andrewoma.kwery.mapper.*
import online.javanese.krud.kwery.KweryTypes
import java.util.*
import kotlin.NoSuchElementException

class ValueFactory<T : Any>(
        table: Table<T, *>,
        private val fallback: Source<*>
) {

    private val converters: Map<String, (String) -> Any> =
            table.allColumns.associateByTo(HashMap(table.allColumns.size), { it.name }, {
                val kweryConverter = it.converter
                val type = KweryTypes.getTypeForConverter(kweryConverter)
                if (type.isEnum) {
                    @Suppress("UPPER_BOUND_VIOLATED")
                    enumAdapterFor<Enum<*>>(type)
                } else {
                    KweryTypes.getConverterForType(type)
                }
            })

    private fun <T : Enum<T>> enumAdapterFor(type: Class<*>): (String) -> T {
        val enumType = type as Class<T>
        return { name: String ->
            java.lang.Enum.valueOf<T>(enumType, name)
        }
    }

    fun from(source: Source<String>): Value<T> =
            object : Value<T> {
                override fun <R> of(column: Column<T, R>): R {
                    val name = column.name
                    return when (name) {
                        in source -> converters[name]!!(source[name]) as R
                        in fallback -> fallback[name] as R
                        else -> column.defaultValue
                    }
                }
            }

    interface Source<T> {
        operator fun contains(key: String): Boolean
        operator fun get(key: String): T
    }

    object EmptySource : Source<Nothing?> {
        override fun contains(key: String): Boolean = false
        override fun get(key: String): Nothing? = throw NoSuchElementException()
    }

}
