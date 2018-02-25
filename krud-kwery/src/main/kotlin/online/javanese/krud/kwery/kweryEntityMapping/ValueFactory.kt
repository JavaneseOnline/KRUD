package online.javanese.krud.kwery.kweryEntityMapping

import com.github.andrewoma.kwery.mapper.*
import io.ktor.util.StringValues
import online.javanese.krud.kwery.KweryTypes
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.NoSuchElementException

/**
 * Creates Kwery mapper's [Value] from given [Source] (map).
 */
class ValueFactory<T : Any>(
        table: Table<T, *>,
        private val fallback: Map<String, *>
) {

    private val converters: Map<
            @ParameterName("columnName") String,
            @ParameterName("converter") (List<String>) -> Any
            > =
            table.allColumns.associateByTo(HashMap(table.allColumns.size), { it.name }, {
                val kweryConverter = it.converter
                val type = KweryTypes.getTypeForConverter(kweryConverter)
                when {
                    type is Class<*> && type.isEnum ->
                        @Suppress("UPPER_BOUND_VIOLATED") enumAdapterFor<Enum<*>>(type)

                    type is Class<*> ->
                        KweryTypes.getConverterForType(type)

                    type is ParameterizedType && type.rawType === Set::class.java && Enum::class.java.isAssignableFrom(type.actualTypeArguments[0] as Class<*>) ->
                        @Suppress("UPPER_BOUND_VIOLATED") enumSetAdapterFor<Enum<*>>(type.actualTypeArguments[0] as Class<*>)

                    else ->
                            error("unsupported type: $type")
                }
            })

    private fun <T : Enum<T>> enumAdapterFor(type: Class<*>): (List<String>) -> T {
        val enumType = type as Class<T>
        return { name ->
            java.lang.Enum.valueOf<T>(enumType, name.single())
        }
    }

    private fun <E : Enum<E>> enumSetAdapterFor(type: Class<*>): (List<String>) -> Set<E> {
        val enumType = type as Class<E>
        return { names ->
            names.mapTo(EnumSet.noneOf(type)) { java.lang.Enum.valueOf<E>(enumType, it) }
        }
    }

    fun from(map: StringValues): Value<T> =
            object : Value<T> {
                override fun <R> of(column: Column<T, R>): R {
                    val name = column.name
                    return when (name) {
                        in map -> converters[name]!!(map.getAll(name)!!) as R
                        in fallback -> fallback[name] as R
                        else -> column.defaultValue
                    }
                }
            }

    interface Source<T> {
        operator fun contains(key: String): Boolean
        operator fun get(key: String): T
    }

}
