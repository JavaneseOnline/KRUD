package online.javanese.krud.crud

import online.javanese.krud.template.Control
import online.javanese.krud.template.EmptyControl
import online.javanese.krud.template.TextInput
import java.util.Collections.unmodifiableList
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty1

/**
 * Represents a table of [E] objects with [ID] primary key.
 */
interface Table<E : Any, ID> {
    /**
     * Url segment
     */
    val route: String

    /**
     * User-visible name which will be shown in a list
     */
    val displayName: String

    /**
     * Total number of [E]s
     */
    val count: Int



    /**
     * Find all [E]s, which will be shown to user as a list.
     */
    fun findAll(): List<E>

    /**
     * Find a single [E] by [ID], or return `null`, if there's no such element
     */
    fun findOne(id: ID): E?

    /**
     * Persist [E]
     * @see createFromMap for details
     */
    fun save(e: E)

    /**
     * Delete [E]. Actually, I hope you'll just set deleted flag
     */
    fun delete(id: ID)

    /**
     * If this table is explicitly sorted,
     * must return [Sort.Explicit] to allow manual reordering
     */
    val sort: Sort<ID>


    /**
     * Extract primary key from [E]
     */
    fun getId(e: E): ID

    /**
     * Extract title, which will be shown to user, from [E]
     */
    fun getTitle(e: E): String

    /**
     * Parse [String] to [ID]
     */
    fun stringToId(s: String): ID

    /**
     * Return all available columns
     */
    val cols: List<Col<E>>

    /**
     * Create new [E] from map.
     * If contains no ID mapping, then it's for insertion. It's for update otherwise.
     */
    fun createFromMap(map: Map<String, String>): E
}

/**
 * Either this table can be sorted by user or not.
 */
sealed class Sort<in ID> {
    object NoneOrImplicit : Sort<Any?>()
    abstract class Explicit<in ID> : Sort<ID>() {
        abstract fun updateOrder(newOrder: List<ID>)
    }
}

/**
 * Table which is being hold in memory, not persisted.
 */
class InMemoryTable<E : Any, ID>(
        override val route: String,
        override val displayName: String,
        private val idOf: (E) -> ID,
        private val titleOf: (E) -> String,
        private val stringToId: (String) -> ID,
        cols: List<Col<E>>,
        items: List<E>,
        private val fromMap: (Map<String, String>) -> E,
        sortable: Boolean
) : Table<E, ID> {

    private val itemsRef = AtomicReference<List<E>>(unmodifiableList(items.toList()))

    override val count: Int get() = itemsRef.get().size

    override fun findAll(): List<E> = itemsRef.get()
    override fun findOne(id: ID): E? = itemsRef.get().singleOrNull { idOf(it) == id }
    override fun save(e: E) {
        val id = idOf(e)
        itemsRef.updateAndGet { items ->
            val idx = items.indexOfFirst { idOf(it) == id }
            val updated = if (idx < 0) items + e else ArrayList(items).also { it[idx] = e }
            unmodifiableList(updated)
        }
    }
    override fun delete(id: ID) {
        itemsRef.updateAndGet { items ->
            unmodifiableList(items.filter { idOf(it) != id })
        }
    }

    override val sort: Sort<ID> = if (sortable) {
        object : Sort.Explicit<ID>() {
            override fun updateOrder(newOrder: List<ID>) {
                itemsRef.updateAndGet { items ->

                    val sorted = newOrder.mapTo(ArrayList(items.size)) { id -> items.single { idOf(it) == id } }
                    if (newOrder.size != items.size) {
                        val unsorted = items.toMutableList()
                        unsorted.removeAll(sorted)
                        sorted.addAll(unsorted)
                    }

                    unmodifiableList(sorted)
                }
            }
        }
    } else {
        Sort.NoneOrImplicit
    }

    override fun getId(e: E): ID = idOf(e)
    override fun getTitle(e: E): String = titleOf(e)

    override fun stringToId(s: String): ID = stringToId.invoke(s)
    override val cols: List<Col<E>> = cols.toList()
    override fun createFromMap(map: Map<String, String>): E = fromMap(map)

}

/**
 * Represents a table column.
 */
interface Col<OWNR : Any> {
    /**
     * Returns value which user will see and, if acceptable, edit
     */
    fun getValue(owner: OWNR): String

    /**
     * Human-readable label
     */
    val name: String

    /**
     * UI control which will be user in Create form
     */
    val createControl: Control

    /**
     * UI control which will be user in Edit form
     */
    val editControl: Control
}

/**
 * Primary key column. Read-only <input type=text>
 */
class IdCol<OWNR : Any, ID>(
        private val property: KProperty1<OWNR, ID>,
        title: String = "ID",
        private val toString: (ID) -> String = Any?::toString
): Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val name: String get() = property.name
    override val createControl: Control get() = EmptyControl
    override val editControl: Control = TextInput(property.name, property.name, title, editable = false)
}

/**
 * Ordinary text column. Editable <input type=text>
 */
class TextCol<OWNR : Any, T>(
        private val property: KProperty1<OWNR, T>,
        title: String = property.name.capitalize(),
        private val toString: (T) -> String = Any?::toString
) : Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val name: String get() = property.name
    private val control: Control = TextInput(property.name, property.name, title)
    override val createControl: Control get() = control
    override val editControl: Control get() = control
}
