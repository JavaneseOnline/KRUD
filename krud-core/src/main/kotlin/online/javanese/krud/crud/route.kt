package online.javanese.krud.crud

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.util.ValuesMap
import online.javanese.krud.WebEnv

/**
 * A route to action which can be performed on a [Table].
 */
class TableActionRoute(
        val method: HttpMethod,
        val keyword: String
) {
    fun addressOf(env: WebEnv, table: Table<*, *>) =
            "${env.routePrefix}/${table.route}/$keyword/"
}

/**
 * Action that can be performed on a table, e. g. list, create new record, etc.
 */
typealias TableAction = suspend (
        env: WebEnv, call: ApplicationCall, table: Table<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit

/**
 * Encapsulates route and table action
 */
class RoutedTableAction(
        val route: TableActionRoute,
        val action: TableAction
)



/**
 * A route to action which can be performed on a record.
 */
class RecordActionRoute(
        val method: HttpMethod,
        val keyword: String
) {
    fun <E : Any> addressOf(env: WebEnv, table: Table<E, *>, record: E) =
            "${env.routePrefix}/${table.route}/$keyword/${table.getId(record)}/"

    fun <E : Any> addressOf(env: WebEnv, tableAndRecord: TableAndRecord<E, *>): String {
        val (table, record) = tableAndRecord
        return addressOf(env, table, record)
    }
}

class TableAndRecord<T : Any, ID>(
        val table: Table<T, ID>,
        val record: T
) {
    operator fun component1() = table
    operator fun component2() = record
}

typealias RecordAction = suspend (
        env: WebEnv, call: ApplicationCall, tableAndRecord: TableAndRecord<*, *>, query: ValuesMap, post: ValuesMap
) -> Unit

/**
 * Encapsulates route and record action
 */
class RoutedRecordAction(
        val route: RecordActionRoute,
        val action: RecordAction
)
