package online.javanese.krud.kwery

import com.github.andrewoma.kwery.mapper.Dao
import io.ktor.util.StringValues
import online.javanese.krud.crud.*
import com.github.andrewoma.kwery.mapper.Column as KwColumn
import com.github.andrewoma.kwery.mapper.Table as KwTable
import online.javanese.krud.crud.Column as KrColumn
import online.javanese.krud.crud.Table as KrTable
import online.javanese.krud.kwery.kweryEntityMapping.ValueFactory
import java.lang.reflect.ParameterizedType

class KweryTable<E : Any, ID : Any>(
        override val route: String,
        private val table: KwTable<E, ID>,
        private val dao: Dao<E, ID>,
        private val getCount: () -> Int,
        private val idFromString: (String) -> ID,
        override val displayName: String = table.name.capitalize(),
        private val getTitleOf: (E) -> String = Any::toString,
        private val columns: Set<KwColumn<E, *>> = table.defaultColumns,
        override val sort: Sort<ID> = Sort.NoneOrImplicit,
        transformColumn: (KwColumn<E, *>) -> KrColumn<E> = TransformKweryColumn(),
        fallback: Map<String, *> = emptyMap<String, Nothing>()
) : KrTable<E, ID> {

    private val idCol = table.idColumns.single()
    private val valueFactory = ValueFactory(table, fallback)

    override fun getTitle(e: E): String = getTitleOf(e)

    override fun getId(e: E): ID = idCol.property(e) as ID

    override fun findAll(): List<E> = dao.findAll(columns)

    override fun findOne(id: ID): E? = dao.findById(id, columns)

    override fun insert(e: E) {
        dao.insert(e)
    }

    override fun update(e: E) {
        dao.unsafeUpdate(e)
    }

    override fun stringToId(s: String): ID = idFromString(s)

    override val cols: List<online.javanese.krud.crud.Column<E>> = columns.map(transformColumn)

    override fun createFrom(map: StringValues): E = table.create(valueFactory.from(map))

    override val count: Int get() = getCount()

    override fun delete(id: ID) {
        dao.delete(id)
    }

    companion object {
        fun <E : Any> TransformKweryColumn(): (KwColumn<E, *>) -> KrColumn<E> = { col: KwColumn<E, *> ->
            val type = try {
                KweryTypes.getTypeForConverter(col.converter)
            } catch (e: NoSuchElementException) {
                null
            }

            when {
                col.id ->
                    IdCol<E, Any?>({ ownr -> col.property(ownr).toString() }, col.name)

                type is Class<*> && type.isBoolean ->
                    BooleanCol({ ownr -> (col.property(ownr) as Boolean).toString() }, col.name)

                type is Class<*> && type.isEnum ->
                    EnumeratedCol({ ownr -> col.property(ownr) as Enum<*> }, col.name, @Suppress("UPPER_BOUND_VIOLATED") eca<Enum<*>>(type))

                type is ParameterizedType && type.rawType === Set::class.java && Enum::class.java.isAssignableFrom(type.actualTypeArguments[0] as Class<*>) ->
                        MultiEnumeratedCol({ ownr -> col.property(ownr) as Set<Enum<*>> }, col.name, @Suppress("UPPER_BOUND_VIOLATED") eca<Enum<*>>(type.actualTypeArguments[0] as Class<*>))

                else ->
                    TextCol<E, Any?>({ ownr -> col.property(ownr).toString() }, col.name)
            }
        }

        private val Class<*>.isBoolean
            get() = this == java.lang.Boolean::class.java || this == java.lang.Boolean.TYPE

        private fun <E : Enum<E>> eca(klass: Class<*>): EnumeratedColAdapter<E, E> = EnumColAdapter(klass as Class<E>)

    }

}
