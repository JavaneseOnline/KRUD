package online.javanese.krud.crud

import online.javanese.krud.template.control.Control
import online.javanese.krud.template.control.ComboBox
import online.javanese.krud.template.control.MultiComboBox
import java.util.*
import kotlin.reflect.KProperty1

/**
 * Describes values of an enumeration.
 */
interface EnumeratedColAdapter<T, TID> {
    fun elements(): Iterable<T>
    fun idOf(t: T): TID
    fun element(id: TID): T
    fun titleOf(element: T): String
}

/**
 * [EnumeratedColAdapter] implementation for [Enum]s.
 */
class EnumColAdapter<E : Enum<E>>(
        klass: Class<E>,
        private val getTitle: (E) -> String = Enum<E>::name
) : EnumeratedColAdapter<E, E> {
    private val el = Collections.unmodifiableList(klass.enumConstants!!.toList())
    override fun elements(): Iterable<E> = el
    override fun idOf(t: E): E = t // enum is ID of itself
    override fun element(id: E): E = id // same
    override fun titleOf(element: E): String = getTitle(element)
}

/**
 * Shortcut factory for [EnumColAdapter]
 */
inline fun <reified E : Enum<E>> EnumColAdapter(
        noinline getTitle: (E) -> String = Enum<E>::name
) = EnumColAdapter(E::class.java, getTitle)


/**
 * Value of such column must be chosen from a provided list.
 * @param T related entity type
 * @param TID T's primary key / ID
 */
class EnumeratedCol<OWNR : Any, T, TID : Any>(
        private val getId: (OWNR) -> TID,
        override val name: String,
        private val adapter: EnumeratedColAdapter<T, TID>,
        private val title: String = name.capitalize(),
        private val idToString: (TID) -> String = Any::toString,
        private val createControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = ComboBox,
        private val editControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = createControlFactory
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, TID>,
            adapter: EnumeratedColAdapter<T, TID>,
            title: String = property.name.capitalize(),
            idToString: (TID) -> String = Any::toString,
            controlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = ComboBox
    ) : this(
            property.getter, property.name, adapter, title, idToString, controlFactory
    )

    override fun getValues(owner: OWNR): List<String> = listOf(idToString(getId(owner)))
    override val createControl: Control get() = adapter.cc(name, title, idToString, createControlFactory)
    override val editControl: Control get() = adapter.cc(name, title, idToString, editControlFactory)

}

/**
 * Allows multiple selection of provided values.
 * @param T related entity type
 * @param TID T's primary key / ID
 */
class MultiEnumeratedCol<OWNR : Any, T, TID : Any>(
        private val getIds: (OWNR) -> Collection<TID>,
        override val name: String,
        private val adapter: EnumeratedColAdapter<T, TID>,
        private val title: String = name.capitalize(),
        private val idToString: (TID) -> String = Any::toString,
        private val createControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = MultiComboBox,
        private val editControlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = createControlFactory
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, Collection<TID>>,
            adapter: EnumeratedColAdapter<T, TID>,
            title: String = property.name.capitalize(),
            idToString: (TID) -> String = Any::toString,
            controlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = MultiComboBox
    ) : this(
            property.getter, property.name, adapter, title, idToString, controlFactory
    )

    override fun getValues(owner: OWNR): List<String> = getIds(owner).map(idToString)
    override val createControl: Control get() = adapter.cc(name, title, idToString, createControlFactory)
    override val editControl: Control get() = adapter.cc(name, title, idToString, editControlFactory)

}

private fun <T, TID : Any> EnumeratedColAdapter<T, TID>.cc(
        name: String, title: String, idToString: (TID) -> String,
        controlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control
): Control {
    val els = elements()
    return controlFactory(name, title, els.map { idToString(idOf(it)) }, els.map(::titleOf))
}
