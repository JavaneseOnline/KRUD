package online.javanese.krud.crud

import online.javanese.krud.template.Control
import online.javanese.krud.template.TextInput
import kotlin.reflect.KProperty1

interface Table<E : Any, ID> {
    val route: String
    val displayName: String
    val count: Int

    fun findAll(): List<E>
    fun findOne(id: ID): E?
    fun getId(e: E): ID
    fun getTitle(e: E): String

    fun stringToId(s: String): ID

    val cols: List<Col<E>>
}

class InMemoryTable<E : Any, ID>(
        override val route: String,
        override val displayName: String,
        private val idOf: (E) -> ID,
        private val titleOf: (E) -> String,
        private val stringToId: (String) -> ID,
        override val cols: List<Col<E>>,
        private val items: List<E>
) : Table<E, ID> {

    override val count: Int get() = items.size

    override fun findAll(): List<E> = items
    override fun findOne(id: ID): E? = items.singleOrNull { idOf(it) == id }
    override fun getId(e: E): ID = idOf(e)
    override fun getTitle(e: E): String = titleOf(e)

    override fun stringToId(s: String): ID = stringToId.invoke(s)

}

interface Col<OWNR : Any> {
    fun getValue(owner: OWNR): String
    val control: Control
}

class IdCol<OWNR : Any, ID>(
        private val property: KProperty1<OWNR, ID>,
        title: String = "ID",
        private val toString: (ID) -> String = Any?::toString
): Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val control: Control = TextInput(property.name, property.name, title, editable = false)
}

class TextCol<OWNR : Any, T>(
        private val property: KProperty1<OWNR, T>,
        title: String = property.name.capitalize(),
        private val toString: (T) -> String = Any?::toString
) : Col<OWNR> {
    override fun getValue(owner: OWNR): String = toString(property.get(owner))
    override val control: Control = TextInput(property.name, property.name, title)
}
