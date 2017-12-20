package online.javanese.krud.crud

import online.javanese.krud.template.Control
import online.javanese.krud.template.ComboBox
import java.util.*
import kotlin.reflect.KProperty1

/**
 * Describes values of an enumeration.
 */
interface EnumeratedColAdapter<T> {
    fun elements(): Iterable<T>
    fun nameOf(t: T): String
    fun titleOf(t: T): String
}

/**
 * [EnumeratedColAdapter] implementation for [Enum]s.
 */
class EnumColAdapter<E : Enum<E>>(
        klass: Class<E>,
        private val getName: (E) -> String = Enum<E>::name,
        private val getTitle: (E) -> String = getName
) : EnumeratedColAdapter<E> {
    private val el = Collections.unmodifiableList(klass.enumConstants!!.toList())
    override fun elements(): Iterable<E> = el
    override fun nameOf(t: E): String = t.name
    override fun titleOf(t: E): String = getTitle(t)
}

/**
 * Shortcut factory for [EnumColAdapter]
 */
inline fun <reified E : Enum<E>> EnumColAdapter(
        noinline getName: (E) -> String = Enum<E>::name,
        noinline getTitle: (E) -> String = getName
) = EnumColAdapter(E::class.java, getName, getTitle)


/**
 * Value of such column must be chosen from a provided list.
 */
class EnumeratedCol<OWNR : Any, T>(
        private val getValue: (OWNR) -> T,
        private val adapter: EnumeratedColAdapter<T>,
        override val name: String,
        private val title: String = name.capitalize(),
        private val controlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = ComboBox
) : Column<OWNR> {

    constructor(
            property: KProperty1<OWNR, T>,
            adapter: EnumeratedColAdapter<T>,
            title: String = property.name.capitalize(),
            controlFactory: (name: String, title: String, names: List<String>, titles: List<String>) -> Control = ComboBox
    ) : this(
            property.getter, adapter, property.name, title, controlFactory
    )

    override fun getValue(owner: OWNR): String = adapter.nameOf(getValue.invoke(owner))
    override val createControl: Control get() = cc()
    override val editControl: Control get() = cc()

    private fun cc(): Control {
        val els = adapter.elements()
        return controlFactory(name, title, els.map(adapter::nameOf), els.map(adapter::titleOf))
    }
}
