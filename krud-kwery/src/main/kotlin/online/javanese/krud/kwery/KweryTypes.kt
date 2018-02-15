package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Converter
import com.github.andrewoma.kwery.mapper.EnumByNameConverter
import com.github.andrewoma.kwery.mapper.standardConverters
import com.github.andrewoma.kwery.mapper.timeConverters
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.Collections.unmodifiableMap
import kotlin.NoSuchElementException

object KweryTypes {

    fun getTypeForConverter(converter: Converter<*>): Class<*> {
        if (converter is EnumByNameConverter<*>) {
            val from = converter.from
            val typeField = from.javaClass.getDeclaredField("\$type")
            typeField.isAccessible = true
            return typeField.get(from) as Class<*>
        }

        converterToType[converter]?.let { return it }

        throw NoSuchElementException("Cannot find type of converter $converter.")
    }


    fun getConverterForType(type: Class<*>) =
            typeToConverter[type]
                    ?: throw NoSuchElementException("No converter for type $type.")

    private val converterToType: Map<Converter<*>, Class<*>>
    private val typeToConverter: Map<Class<*>, (String) -> Any>

    init {
        val kweryConverters =
                standardConverters + timeConverters + (Uuid::class.java to UuidConverter)

        converterToType = unmodifiableMap(kweryConverters
                .map { (k, v) -> k to v }
                .associateByTo(HashMap(kweryConverters.size), { it.second }, { it.first }))

        val BooleanConverter: (String) -> Boolean = java.lang.Boolean::parseBoolean
        val ByteConverter: (String) -> Byte = java.lang.Byte::parseByte
        val ShortConverter: (String) -> Short = java.lang.Short::parseShort
        val IntConverter: (String) -> Int = java.lang.Integer::parseInt
        val LongConverter: (String) -> Long = java.lang.Long::parseLong
        val FloatConverter: (String) -> Float = java.lang.Float::parseFloat
        val DoubleConverter: (String) -> Double = java.lang.Double::parseDouble

        typeToConverter = unmodifiableMap(mapOf(
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
                BigDecimal::class.java to ::BigDecimal,
                String::class.java to { s: String -> s },
                // ByteArray, Timestamp, Time, Date ignored

                // timeConverters
                LocalDateTime::class.java to LocalDateTime::parse,
                LocalDate::class.java to LocalDate::parse,
                LocalTime::class.java to LocalTime::parse,
                Instant::class.java to Instant::parse,
                // several converters ignored

                Uuid::class.java to UUID::fromString
        ))
    }

}