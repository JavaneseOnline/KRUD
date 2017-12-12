package online.javanese.krud.crud

import java.util.Collections.unmodifiableList
import java.util.concurrent.atomic.AtomicReference

/**
 * Table which is being hold in memory, not persisted.
 */
class InMemoryTable<E : Any, ID>(
        override val route: String,
        override val displayName: String,
        private val idOf: (E) -> ID,
        private val titleOf: (E) -> String,
        private val stringToId: (String) -> ID,
        cols: List<Column<E>>,
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
    override val cols: List<Column<E>> = cols.toList()
    override fun createFromMap(map: Map<String, String>): E = fromMap(map)

}
