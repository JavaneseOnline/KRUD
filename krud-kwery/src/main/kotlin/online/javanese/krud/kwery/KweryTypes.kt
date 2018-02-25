package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Converter
import com.github.andrewoma.kwery.mapper.EnumByNameConverter
import com.github.andrewoma.kwery.mapper.standardConverters
import com.github.andrewoma.kwery.mapper.timeConverters
import java.lang.reflect.Type
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.Collections.unmodifiableMap
import kotlin.NoSuchElementException


object KweryTypes {

    fun getTypeForConverter(converter: Converter<*>): Type {
        if (converter is EnumByNameConverter<*>) {
            val from = converter.from
            val typeField = from.javaClass.getDeclaredField("\$type")
            typeField.isAccessible = true
            return typeField.get(from) as Class<*>
        }

        converterToType[converter]?.let { return it }

        try {
            return converter.javaClass.getDeclaredMethod("getType")(converter) as Type
        } catch (ignored: ReflectiveOperationException) {
        }

        throw NoSuchElementException("Cannot find type of converter $converter.")
    }


    fun getConverterForType(type: Class<*>) =
            typeToConverter[type]
                    ?: throw NoSuchElementException("No converter for type $type.")

    private val converterToType: Map<Converter<*>, Class<*>>
    private val typeToConverter: Map<Class<*>, (List<String>) -> Any>

    init {
        val kweryConverters =
                standardConverters + timeConverters + (Uuid::class.java to UuidConverter)

        converterToType = unmodifiableMap(kweryConverters
                .map { (k, v) -> k to v }
                .associateByTo(HashMap(kweryConverters.size), { it.second }, { it.first }))

        val BooleanConverter: (List<String>) -> Boolean = { java.lang.Boolean.parseBoolean(it.single()) }
        val ByteConverter: (List<String>) -> Byte = { java.lang.Byte.parseByte(it.single()) }
        val ShortConverter: (List<String>) -> Short = { java.lang.Short.parseShort(it.single()) }
        val IntConverter: (List<String>) -> Int = { java.lang.Integer.parseInt(it.single()) }
        val LongConverter: (List<String>) -> Long = { java.lang.Long.parseLong(it.single()) }
        val FloatConverter: (List<String>) -> Float = { java.lang.Float.parseFloat(it.single()) }
        val DoubleConverter: (List<String>) -> Double = { java.lang.Double.parseDouble(it.single()) }

        typeToConverter = unmodifiableMap(mapOf<Class<*>, (List<String>) -> Any>(
                // standardConverters
                java.lang.Boolean::class.java to BooleanConverter,
                java.lang.Boolean.TYPE to BooleanConverter,
                java.lang.Byte::class.java to ByteConverter,
                java.lang.Byte.TYPE to ByteConverter,
                java.lang.Short::class.java to ShortConverter,
                java.lang.Short.TYPE to ShortConverter,
                java.lang.Integer::class.java to IntConverter,
                java.lang.Integer.TYPE to IntConverter,
                java.lang.Long::class.java to LongConverter,
                java.lang.Long.TYPE to LongConverter,
                java.lang.Float::class.java to FloatConverter,
                java.lang.Float.TYPE to FloatConverter,
                java.lang.Double::class.java to DoubleConverter,
                java.lang.Double.TYPE to DoubleConverter,
                BigDecimal::class.java to { it: List<String> -> BigDecimal(it.single()) },
                String::class.java to List<String>::single,
                // ByteArray, Timestamp, Time, Date ignored

                // timeConverters
                LocalDateTime::class.java to { it: List<String> -> LocalDateTime.parse(it.single()) },
                LocalDate::class.java to { it: List<String> -> LocalDate.parse(it.single()) },
                LocalTime::class.java to { it: List<String> -> LocalTime.parse(it.single()) },
                Instant::class.java to { it: List<String> -> Instant.parse(it.single()) },
                // several converters ignored

                Uuid::class.java to { it: List<String> -> Uuid.fromString(it.single()) }
        ))
    }

}