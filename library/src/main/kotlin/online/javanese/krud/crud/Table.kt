package online.javanese.krud.crud

import online.javanese.krud.template.Control
import online.javanese.krud.template.TextInput
import java.util.Collections.unmodifiableList
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty1

interface Table<E : Any, ID> {
    val route: String
    val displayName: String
    val count: Int

    fun findAll(): List<E>
    fun findOne(id: ID): E?
    fun save(e: E)

    fun getId(e: E): ID
    fun getTitle(e: E): String

    fun stringToId(s: String): ID
    val cols: List<Col<E>>
    fun createFromMap(map: Map<String, String>): E
}

class InMemoryTable<E : Any, ID>(
        override val route: String,
        override val displayName: String,
        private val idOf: (E) -> ID,
        private val titleOf: (E) -> String,
        private val stringToId: (String) -> ID,
        cols: List<Col<E>>,
        items: List<E>,
        private val fromMap: (Map<String, String>) -> E
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

    override fun getId(e: E): ID = idOf(e)
    override fun getTitle(e: E): String = titleOf(e)

    override fun stringToId(s: String): ID = stringToId.invoke(s)
    override val cols: List<Col<E>> = cols.toList()
    override fun createFromMap(map: Map<String, String>): E = fromMap(map)

}

interface Col<OWNR : Any> {
    fun getValue(owner: OWNR): String
    val name: String
    val createControl: Control?
    val editControl: Control?
}

class IdCol<OWNR : Any, ID>(
        private val property: KProperty1<OWNR, ID>,
        title: String = "ID",
        private val toString: (ID) -> String = Any?::toString
): Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val name: String get() = property.name
    override val createControl: Control? get() = null
    override val editControl: Control? = TextInput(property.name, property.name, title, editable = false)
}

class TextCol<OWNR : Any, T>(
        private val property: KProperty1<OWNR, T>,
        title: String = property.name.capitalize(),
        private val toString: (T) -> String = Any?::toString
) : Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val name: String get() = property.name
    private val control: Control = TextInput(property.name, property.name, title)
    override val createControl: Control? get() = control
    override val editControl: Control? get() = control
}
