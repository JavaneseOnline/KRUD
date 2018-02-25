package online.javanese.krud.crud

import io.ktor.util.StringValues
import io.ktor.util.StringValuesImpl

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
     * Persist [E] as a new record.
     * @see createFrom for details
     */
    fun insert(e: E)

    /**
     * Update [E], i. e. mutate existing record.
     * @see createFrom for details
     */
    fun update(e: E)

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
    val cols: List<Column<E>>

    /**
     * Create new [E] from map.
     * If contains no ID mapping, then it's for insertion. It's for update otherwise.
     */
    fun createFrom(map: StringValues): E
}

internal fun <E : Any> Table<E, *>.toMap(e: E): StringValues =
        StringValuesImpl(values = cols.associateBy({ it.name }, { it.getValues(e) }))

/**
 * Either this table can be sorted by user or not.
 */
sealed class Sort<in ID> {

    /**
     * There's no explicit record sorting.
     */
    object NoneOrImplicit : Sort<Any?>()

    /**
     * Records are sorted explicitly and thus may be reordered by user.
     */
    abstract class Explicit<in ID> : Sort<ID>() {

        /**
         * Should reorder table rows in the specified order.
         */
        abstract fun updateOrder(newOrder: List<ID>)
    }
}
